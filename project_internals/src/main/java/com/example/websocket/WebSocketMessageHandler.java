package com.example.websocket;
import com.example.websocket.ProtocolMapper;
import main.java.com.example.GameState;

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

    public WebSocketMessageHandler(/*GameService gameService*/) {
        //this.gameService = gameService;
    }

    /**
     * Handle a single incoming JSON message.
     *
     * @param json incoming JSON from client
     * @param session the WebSocket session (if you want to use it for context)
     * @return a JSON string to send back to this client, or null if nothing to send directly
     */
public String handleIncoming(String json, Object sessionContext) {
    WsEnvelope msg = JsonSupport.decode(json, WsEnvelope.class);

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
            case PING:
                response = JsonSupport.encode(WsResponses.pong(msg.requestId));
                break;

            case CREATE_GAME:
                // if CREATE_GAME requires playerId, validate here
                // validateCreateGame(msg);
                response = handleCreateGame(msg);
                break;

            case JOIN_GAME:
                // validateJoinGame(msg);
                response = handleJoinGame(msg);
                break;

            case START_GAME:
                // validateStartGame(msg);
                response = handleStartGame(msg);
                break;

            case PLAYER_MOVE:
                // validatePlayerMove(msg);
                response = handlePlayerMove(msg);
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

    private String handleCreateGame(WsEnvelope msg) {
        /*GameConfig config = ProtocolMapper.toGameConfig(msg);*/
        /*GameState state = gameService.createGame(msg.playerId, config);*/
        //String type = "CREATE_GAME";
        String config = null;
        String state = "PENDING";
        return JsonSupport.encode(ProtocolMapper.toGameCreatedMessage(msg.requestId, state));
    }

    public String handleJoinGame(WsEnvelope msg) {
    try {
        validateJoinGame(msg);

        //Object state = gameService.joinGame(msg.playerId, msg.gameId);
        String state = "READYTOSTART";

        return JsonSupport.encode(
            ProtocolMapper.toGameStateMessage(msg.requestId, state, msg.gameId,msg.playerId)
        );

    } catch (IllegalArgumentException e) {
        return JsonSupport.encode(
            ProtocolMapper.toErrorMessage(
                msg.requestId,
                "BAD_REQUEST",
                e.getMessage()
            )
        );
    } catch (Exception e) {
        return JsonSupport.encode(
            ProtocolMapper.toErrorMessage(
                msg.requestId,
                "SERVER_ERROR",
                "Join game failed"
            )
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

    private String handleStartGame(WsEnvelope msg) {
        /*GameState state = gameService.startGame(msg.playerId, msg.gameId);*/
        String state = null;
        return JsonSupport.encode(ProtocolMapper.toGameStartedMessage(msg.requestId, state,msg.gameId,msg.playerId));
    }

    private String handlePlayerMove(WsEnvelope msg) {
        System.out.println("[APPLY MOVE] player=" + msg.playerId
        + " req=" + msg.requestId);
        long fromId = ((Number) msg.payload.get("fromArticleId")).longValue();
        long toId   = ((Number) msg.payload.get("toArticleId")).longValue();

        /*GameState state = gameService.applyMove(msg.playerId, msg.gameId, fromId, toId);*/
        String state = null;
        return JsonSupport.encode(ProtocolMapper.toMoveResultMessage(msg.requestId, state, msg.playerId,fromId,toId));
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