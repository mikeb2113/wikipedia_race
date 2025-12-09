package com.example.websocket;

import java.util.List;
import java.util.Map;

public class WsResponses {

    public static Map<String, Object> error(
            String requestId,
            String code,
            String message,
            String requestType
    ) {
        return Map.of(
            "type", "ERROR",
            "requestId", requestId,
            "code", code,
            "message", message,
            "requestType", requestType
        );
    }

    public static Map<String, Object> pong(String requestId) {
        return Map.of(
            "type", "PONG",
            "requestId", requestId
        );
    }

    public static Map<String, Object> gameCreated(
            String requestId,
            String gameId,
            String ownerId
    ) {
        return Map.of(
            "type", "GAME_CREATED",
            "requestId", requestId,
            "gameId", gameId,
            "ownerId", ownerId
        );
    }

    // Stubbed examples; you’ll replace with real fields

    public static Map<String, Object> gameStateStub(String requestId, String gameId) {
        return Map.of(
            "type", "GAME_STATE",
            "requestId", requestId,
            "gameId", gameId,
            "phase", "LOBBY",
            "players", List.of()
        );
    }

    public static Map<String, Object> gameStartedStub(String requestId, String gameId) {
        return Map.of(
            "type", "GAME_STARTED",
            "requestId", requestId,
            "gameId", gameId
        );
    }

    public static Map<String, Object> moveResultStub(String requestId, String gameId, String playerId) {
        return Map.of(
            "type", "MOVE_RESULT",
            "requestId", requestId,
            "gameId", gameId,
            "playerId", playerId,
            "valid", true,
            "isWinningMove", false
        );
    }
}
