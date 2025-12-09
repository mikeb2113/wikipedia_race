package com.example.websocket;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal ProtocolMapper so the project compiles.
 * Later you will replace these methods with real transformations
 * from WsEnvelope -> GameService DTOs -> WebSocket response objects.
 */
public class ProtocolMapper {

    /**
     * Produces a minimal "game created" message.
     */
    public static Map<String, Object> toGameCreatedMessage(String requestId, Object state) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "GAME_CREATED");
        msg.put("requestId", requestId);
        msg.put("state", state); // placeholder — typically you'd map fields
        return msg;
    }

    /**
     * Produces a minimal "game state" message.
     */
    public static Map<String, Object> toGameStateMessage(String requestId, Object state) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "GAME_STATE");
        msg.put("requestId", requestId);
        msg.put("state", state);
        return msg;
    }

    /**
     * Produces a minimal "game started" message.
     */
    public static Map<String, Object> toGameStartedMessage(String requestId, Object state) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "GAME_STARTED");
        msg.put("requestId", requestId);
        msg.put("state", state);
        return msg;
    }

    /**
     * Produces a minimal "move result" message.
     */
    public static Map<String, Object> toMoveResultMessage(String requestId, Object state, String playerId) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "MOVE_RESULT");
        msg.put("requestId", requestId);
        msg.put("playerId", playerId);
        msg.put("state", state);
        return msg;
    }
}