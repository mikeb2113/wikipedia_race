package com.example.websocket;
import com.example.websocket.ProtocolMapper;
//import jakarta.websocket.Session;
// or use Object if you want it to be framework-agnostic

public class WebSocketMessageHandler {

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

    String response;

    switch (msg.type) {
        case PING:
            response = JsonSupport.encode(WsResponses.pong(msg.requestId));
            break;

        case CREATE_GAME:
            response = handleCreateGame(msg);
            break;

        case JOIN_GAME:
            response = handleJoinGame(msg);
            break;

        case START_GAME:
            response = handleStartGame(msg);
            break;

        case PLAYER_MOVE:
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

    return response;
}

    private String handleCreateGame(WsEnvelope msg) {
        /*GameConfig config = ProtocolMapper.toGameConfig(msg);*/
        /*GameState state = gameService.createGame(msg.playerId, config);*/
        String config = null;
        String state = null;
        return JsonSupport.encode(ProtocolMapper.toGameCreatedMessage(msg.requestId, state));
    }

    private String handleJoinGame(WsEnvelope msg) {
        /*GameState state = gameService.joinGame(msg.playerId, msg.gameId);*/
        String state = null;
        return JsonSupport.encode(ProtocolMapper.toGameStateMessage(msg.requestId, state));
    }

    private String handleStartGame(WsEnvelope msg) {
        /*GameState state = gameService.startGame(msg.playerId, msg.gameId);*/
        String state = null;
        return JsonSupport.encode(ProtocolMapper.toGameStartedMessage(msg.requestId, state));
    }

    private String handlePlayerMove(WsEnvelope msg) {
        long fromId = ((Number) msg.payload.get("fromArticleId")).longValue();
        long toId   = ((Number) msg.payload.get("toArticleId")).longValue();

        /*GameState state = gameService.applyMove(msg.playerId, msg.gameId, fromId, toId);*/
        String state = null;
        return JsonSupport.encode(ProtocolMapper.toMoveResultMessage(msg.requestId, state, msg.playerId));
    }
}