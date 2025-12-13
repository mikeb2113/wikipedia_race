package com.example.websocket;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

public class ProtocolMapper {

    /**
     * Produces a minimal "game created" message.
     */
    public static Map<String, Object> toGameCreatedMessage(String requestId, Object state, String gameId, String playerId) {
    Map<String, Object> msg = new HashMap<>();
    msg.put("type", "GAME_CREATED");
    msg.put("requestId", requestId);
    msg.put("gameId", gameId);
    msg.put("playerId", playerId);
    msg.put("state", state);
    return msg;
}

public static Map<String, Object> toGameCreatedEvent(String requestId, String gameId, String playerId) {
    Map<String, Object> msg = new HashMap<>();
    msg.put("type", "GAME_CREATED_EVENT");
    msg.put("requestId", requestId);
    msg.put("gameId", gameId);
    msg.put("playerId", playerId);
    return msg;
}
    /**
     * Produces a minimal "game state" message.
     */
    public static Map<String, Object> toGameStateMessage(
      String requestId, Object state,
      String gameId, String playerId) 
    {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "GAME_STATE");
        msg.put("requestId", requestId);
        msg.put("state", state);
        msg.put("gameId",gameId);
        msg.put("playerId",playerId);
        return msg;
    }

    /**
     * Produces a minimal "game started" message.
     */
    public static Map<String, Object> toGameStartedMessage(
      String requestId, Object state, 
      String gameId, String playerId) 
      {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "GAME_STARTED");
        msg.put("requestId", requestId);
        msg.put("state", state);
        msg.put("gameId",gameId);
        msg.put("playerId",playerId);
        return msg;
    }

    public static Map<String, Object> toPlayerJoinedMessage(String requestId, String playerId, String gameId) {
    Map<String, Object> msg = new HashMap<>();
    msg.put("type", "PLAYER_JOINED");
    msg.put("requestId", requestId);
    msg.put("playerId", playerId);
    msg.put("gameId", gameId);
    return msg;
}

    /**
     * Produces a minimal "move result" message.
     */
    public static Map<String, Object> toMoveResultMessage(
            String requestId, Object state, String playerId,
            String gameId, long fromId, long toId
    ) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "MOVE_RESULT");
        msg.put("requestId", requestId);
        msg.put("playerId", playerId);
        msg.put("gameId", gameId);
        msg.put("state", state);
        msg.put("fromArticleId", fromId);
        msg.put("toArticleId", toId);
        return msg;
    }

    public static Map<String, Object> toErrorMessage(
            String requestId,
            String errorCode,
            String errorMessage
    ) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "ERROR");
        msg.put("requestId", requestId);
        msg.put("errorCode", errorCode);
        msg.put("errorMessage", errorMessage);
        return msg;
    }
  }
    /*
    Sample move websocket:
    const ws = new WebSocket("ws://localhost:8080");

    ws.onopen = () => {
    console.log("connected");

    ws.send(JSON.stringify({
        type: "PLAYER_MOVE",
        requestId: "MOVE_TEST_1",
        playerId: "player-123",
        gameId: null,
        payload: {
            fromArticleId: 5,
            toArticleId: 7,
    }
    }));
    };

    ws.onmessage = (event) => {
    console.log("server says:", event.data);
    };
    */