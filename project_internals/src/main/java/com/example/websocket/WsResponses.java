package com.example.websocket;

import com.example.persistence.DTOs.GameStateDTO;
import com.example.persistence.DTOs.PlayerDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WsResponses {

    private static Map<String, Object> base(String type, String requestId) {
        Map<String, Object> env = new HashMap<>();
        env.put("type", type);
        if (requestId != null) {
            env.put("requestId", requestId);
        }
        return env;
    }

    public static Map<String, Object> error(
            String requestId,
            String code,
            String message,
            String requestType
    ) {
        Map<String, Object> env = base("ERROR", requestId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("code", code);
        payload.put("message", message);
        if (requestType != null) {
            payload.put("requestType", requestType);
        }
        env.put("payload", payload);
        return env;
    }

    public static Map<String, Object> pong(String requestId) {
        Map<String, Object> env = base("PONG", requestId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("serverTime", System.currentTimeMillis());
        env.put("payload", payload);
        return env;
    }

    // ===== Game-related =====

    /** After CREATE_GAME succeeds */
    public static Map<String, Object> gameCreated(String requestId, GameStateDTO state) {
        // You can either send a small payload, or reuse full gameState
        Map<String, Object> env = base("GAME_CREATED", requestId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("gameId", state.gameId);
        payload.put("ownerId", state.ownerId);
        payload.put("phase", state.phase);
        env.put("payload", payload);
        return env;
    }

    /** Full game snapshot (after JOIN, START, MOVE, etc.) */
    public static Map<String, Object> gameState(String requestId, GameStateDTO state) {
        Map<String, Object> env = base("GAME_STATE", requestId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("gameId", state.gameId);
        payload.put("ownerId", state.ownerId);
        payload.put("phase", state.phase);
        payload.put("startArticleId", state.startArticleId);
        payload.put("endArticleId", state.endArticleId);
        payload.put("currentTurnPlayerId", state.currentTurnPlayerId);

        // Convert List<PlayerDTO> -> List<Map<...>>
        List<Map<String, Object>> players = state.players.stream()
                .map(WsResponses::toPlayerMap)
                .collect(Collectors.toList());
        payload.put("players", players);

        env.put("payload", payload);
        return env;
    }

    /** When the game transitions to IN_PROGRESS */
    public static Map<String, Object> gameStarted(String requestId, GameStateDTO state) {
        Map<String, Object> env = base("GAME_STARTED", requestId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("gameId", state.gameId);
        payload.put("phase", state.phase); // should now be "IN_PROGRESS"
        payload.put("startTime", System.currentTimeMillis());
        env.put("payload", payload);
        return env;
    }

    /** After a move: you might include both move result and updated state later */
    public static Map<String, Object> moveResult(
            String requestId,
            GameStateDTO state,
            String playerId,
            boolean valid,
            boolean isWinningMove,
            String reason
    ) {
        Map<String, Object> env = base("MOVE_RESULT", requestId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("gameId", state.gameId);
        payload.put("playerId", playerId);
        payload.put("valid", valid);
        payload.put("isWinningMove", isWinningMove);
        if (!valid && reason != null) {
            payload.put("reason", reason);
        }

        env.put("payload", payload);
        return env;
    }

    private static Map<String, Object> toPlayerMap(PlayerDTO p) {
        Map<String, Object> m = new HashMap<>();
        m.put("playerId", p.playerId);
        m.put("displayName", p.displayName);
        m.put("isOwner", p.isOwner);
        m.put("currentArticleId", p.currentArticleId);
        m.put("moveCount", p.moveCount);
        return m;
    }
}