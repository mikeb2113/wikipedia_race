package com.example.websocket;
import com.example.websocket.ConnectionRegistry;
import com.example.websocket.ProtocolMapper;
import main.java.com.example.GameState;
import main.java.com.example.core.domain.GameCommandService;

//import java.net.http.WebSocket;
import org.java_websocket.WebSocket;
import java.time.LocalDateTime;
//import jakarta.websocket.Session;
// or use Object if you want it to be framework-agnostic
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WebSocketMessageHandler {

    private static final long IDEMPOTENCY_TTL_MS = 30_000; // 30s is plenty for retries
    private final ConcurrentMap<RequestKey, CachedResponse> responseCache = new ConcurrentHashMap<>();
    private final GameCommandService gameService;

    /*public WebSocketMessageHandler(String message, WebSocket conn, ConnectionRegistry registry) {
        //this.gameService = gameService;
    }*/

    public WebSocketMessageHandler(GameCommandService gameService) {
        this.gameService = gameService;
    }

    private String cachedOrNull(WsEnvelope msg) {
        if (msg == null || msg.playerId == null || msg.requestId == null || msg.type == null)
            return null;

        RequestKey key = new RequestKey(msg.playerId, msg.requestId, msg.type);
        CachedResponse cached = responseCache.get(key);
        if (cached == null) return null;

        long now = System.currentTimeMillis();
        if (cached.expiresAtMs < now) {
            responseCache.remove(key);
            return null;
        }

        return cached.json;
    }

    private void cachePut(WsEnvelope msg, String responseJson) {
    if (msg == null || msg.playerId == null || msg.requestId == null || msg.type == null) return;
    long expiresAt = System.currentTimeMillis() + IDEMPOTENCY_TTL_MS;
    responseCache.put(new RequestKey(msg.playerId, msg.requestId, msg.type),
                      new CachedResponse(responseJson, expiresAt));
}

private void cleanupExpired() {
    long now = System.currentTimeMillis();
    responseCache.entrySet().removeIf(
        e -> e.getValue().expiresAtMs < now
    );
}

// minimal common validation
private void validateCommon(WsEnvelope msg) {
    if (msg == null) throw new IllegalArgumentException("null msg");
    if (msg.type == null) throw new IllegalArgumentException("Missing type");
    if (msg.requestId == null || msg.requestId.isBlank())
        throw new IllegalArgumentException("Missing requestId");
    // playerId can be optional for PING depending on your protocol.
}

    //private final GameService gameService;

    /**
     * Handle a single incoming JSON message.
     *
     * @param json incoming JSON from client
     * @param session the WebSocket session (if you want to use it for context)
     * @return a JSON string to send back to this client, or null if nothing to send directly
     */
public String handleIncoming(String json, Object sessionContext, ConnectionRegistry registry) {
    WsEnvelope msg = JsonSupport.decode(json, WsEnvelope.class);
    org.java_websocket.WebSocket conn = (org.java_websocket.WebSocket) sessionContext;

    // Validate basics early (so msg.type/requestId exists for error responses)
    try {
        validateCommon(msg);
    } catch (IllegalArgumentException e) {
        return JsonSupport.encode(
            WsResponses.error(
                msg != null ? msg.requestId : null,
                "BAD_REQUEST",
                e.getMessage(),
                msg != null && msg.type != null ? msg.type.name() : null
            )
        );
    }

    if (msg.playerId != null) registry.bindPlayer(conn, msg.playerId);
    if (msg.gameId != null)   registry.bindGame(conn, msg.gameId);

    // Idempotency: if client retries same requestId, return same response
    String cached = cachedOrNull(msg);
    if (cached != null) {
        System.out.println("[IDEMPOTENCY HIT] " + msg.type + " player=" + msg.playerId
            + " req=" + msg.requestId + " game=" + msg.gameId);
        return cached;
    }
    if (cached != null) return cached;

    String response;

    try {
        switch (msg.type) {
            //FINISHED
            case PING:
                response = JsonSupport.encode(WsResponses.pong(msg.requestId));
                break;

            case CREATE_GAME:
                // FINISHED
                response = handleCreateGame(msg, conn, registry);
                break;

            case JOIN_GAME:
                // FINISHED
                response = handleJoinGame(msg,conn,registry);
                break;

                //FINISHED
            case START_GAME:
                // validateStartGame(msg);
                response = handleStartGame(msg, conn, registry);
                break;

                //FINISHED
            case PLAYER_MOVE:
                // validatePlayerMove(msg);
                response = handlePlayerMove(msg, conn, registry);
                break;

            default:
                response = JsonSupport.encode(
                    WsResponses.error(
                        msg.requestId,
                        "UNKNOWN_TYPE",
                        "Unsupported type: " + msg.type,
                        msg.type != null ? msg.type.name() : null
                    )
                );
                break;
        }
    } catch (IllegalArgumentException e) {
        response = JsonSupport.encode(
            WsResponses.error(
                msg.requestId,
                "BAD_REQUEST",
                e.getMessage(),
                msg.type != null ? msg.type.name() : null
            )
        );
    } catch (Exception e) {
        response = JsonSupport.encode(
            WsResponses.error(
                msg.requestId,
                "SERVER_ERROR",
                "Unhandled server error",
                msg.type != null ? msg.type.name() : null
            )
        );
    }

    // Cache the response for retries (optional: skip caching PING)
    if (msg.type != MessageType.PING) {
        cachePut(msg, response);
        // Optional: don’t do every time if high traffic; but fine for your project
        cleanupExpired();
    }

    return response;
}

    private String handleCreateGame(WsEnvelope msg, WebSocket conn, ConnectionRegistry registry) {
    // validateCreateGame(msg) if you want (requestId/playerId)

    // TODO: GameService should generate this and persist it
    String gameId = (msg.gameId != null && !msg.gameId.isBlank())
            ? msg.gameId
            : "game-" + System.currentTimeMillis();

    String state = "PENDING";

    // Ensure registry knows this connection is in the new game
    registry.bindPlayer(conn, msg.playerId);
    registry.bindGame(conn, gameId);

    // 1) Broadcast event: GAME_CREATED (useful if you support lobby / spectators)
    String createdEventJson = JsonSupport.encode(
        ProtocolMapper.toGameCreatedEvent(msg.requestId, gameId, msg.playerId)
    );
    int sent = registry.broadcastToGame(gameId, createdEventJson);
    System.out.println("[BROADCAST] GAME_CREATED game=" + gameId + " sent=" + sent);

    // 2) Return GAME_CREATED response to the creator (includes gameId)
    return JsonSupport.encode(
        ProtocolMapper.toGameCreatedMessage(msg.requestId, state, gameId, msg.playerId)
    );
}

    public String handleJoinGame(WsEnvelope msg, WebSocket conn, ConnectionRegistry registry) {
    try {
        validateJoinGame(msg);
        System.out.println("Joining game...");

        // TODO: state = gameService.joinGame(msg.playerId, msg.gameId);
        String state = "READYTOSTART";

        // 1) Broadcast an EVENT (not GAME_STATE) so others know someone joined
        String joinedEventJson = JsonSupport.encode(
            ProtocolMapper.toPlayerJoinedMessage(msg.requestId, msg.playerId, msg.gameId)
        );
        int sent = registry.broadcastToGame(msg.gameId, joinedEventJson);
        System.out.println("[BROADCAST] PLAYER_JOINED game=" + msg.gameId + " sent=" + sent);

        // 2) Return GAME_STATE to the joiner only (single copy)
        return JsonSupport.encode(
            ProtocolMapper.toGameStateMessage(msg.requestId, state, msg.gameId, msg.playerId)
        );

    } catch (IllegalArgumentException e) {
        return JsonSupport.encode(
            ProtocolMapper.toErrorMessage(msg.requestId, "BAD_REQUEST", e.getMessage())
        );
    } catch (Exception e) {
        return JsonSupport.encode(
            ProtocolMapper.toErrorMessage(msg.requestId, "SERVER_ERROR", "Join game failed")
        );
    }
}


    private void validateJoinGame(WsEnvelope msg) {
        if (msg == null) throw new IllegalArgumentException("null msg");
        if (msg.requestId == null || msg.requestId.isBlank())
            throw new IllegalArgumentException("Missing requestId");
        if (msg.playerId == null || msg.playerId.isBlank())
            throw new IllegalArgumentException("Missing playerId");
        if (msg.gameId == null || msg.gameId.isBlank())
            throw new IllegalArgumentException("Missing gameId");
    }

    private String handleStartGame(WsEnvelope msg, WebSocket conn, ConnectionRegistry registry) {
    // validateStartGame(msg) recommended
    Object state = null; // TODO gameService.startGame(...)

    String startedJson = JsonSupport.encode(
        ProtocolMapper.toGameStartedMessage(msg.requestId, state, msg.gameId, msg.playerId)
    );

    // Broadcast to everyone else (not the starter)
    int sent = registry.broadcastToGameExcept(msg.gameId, conn, startedJson);
    System.out.println("[BROADCAST] GAME_STARTED game=" + msg.gameId + " sent=" + sent + " (excluding starter)");

    // Return to starter only
    return startedJson;
    }

    private String handlePlayerMove(WsEnvelope msg, WebSocket conn, ConnectionRegistry registry) {
    // Optional: validateMove(msg) (playerId, gameId, payload fields)
    System.out.println("[APPLY MOVE] player=" + msg.playerId + " req=" + msg.requestId);

    long fromId = ((Number) msg.payload.get("fromArticleId")).longValue();
    long toId   = ((Number) msg.payload.get("toArticleId")).longValue();

    // TODO: Object state = gameService.applyMove(msg.playerId, msg.gameId, fromId, toId);
    Object state = null; // placeholder for now

    // 1) Broadcast updated state to everyone (EVENT)
    String stateJson = JsonSupport.encode(
        ProtocolMapper.toGameStateMessage(msg.requestId, state, msg.gameId, msg.playerId)
    );

    int sent = registry.broadcastToGame(msg.gameId, stateJson);
    System.out.println("[BROADCAST] GAME_STATE game=" + msg.gameId + " sent=" + sent);

    // 2) Return move result to mover (RESPONSE)
    return JsonSupport.encode(
        ProtocolMapper.toMoveResultMessage(msg.requestId, state, msg.playerId,msg.gameId, fromId, toId)
    );
}

private void validateStartGame(WsEnvelope msg) {
    validateCommon(msg);
    if (msg.playerId == null || msg.playerId.isBlank()) throw new IllegalArgumentException("Missing playerId");
    if (msg.gameId == null || msg.gameId.isBlank()) throw new IllegalArgumentException("Missing gameId");
}
    private static final class CachedResponse {
        final String json;
        final long expiresAtMs;

        CachedResponse(String json, long expiresAtMs) {
            this.json = json;
            this.expiresAtMs = expiresAtMs;
        }
    }

    private static final class RequestKey {
        private final String playerId;
        private final String requestId;
        private final MessageType type;

        private RequestKey(String playerId, String requestId, MessageType type) {
            this.playerId = playerId;
            this.requestId = requestId;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RequestKey)) return false;
            RequestKey other = (RequestKey) o;
            return Objects.equals(playerId, other.playerId)
                && Objects.equals(requestId, other.requestId)
                && type == other.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(playerId, requestId, type);
        }
    }
}

    /*
    Create sample game like:
        const ws = new WebSocket("ws://localhost:8080");

        ws.onopen = () => {
        console.log("connected");

        ws.send(JSON.stringify({
            type: "CREATE_GAME",
            requestId: "create_game_test_1",
            playerId: "player-123",
            gameId: null,
            payload: {}
        }));
        };

        ws.onmessage = (event) => {
        console.log("server says:", event.data);
        };
    */