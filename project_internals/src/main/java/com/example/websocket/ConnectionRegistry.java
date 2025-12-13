package com.example.websocket;

import org.java_websocket.WebSocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionRegistry {

    // conn -> playerId, gameId
    private final ConcurrentMap<WebSocket, String> connToPlayer = new ConcurrentHashMap<>();
    private final ConcurrentMap<WebSocket, String> connToGame   = new ConcurrentHashMap<>();

    // playerId -> conn (simple: 1 connection per player)
    private final ConcurrentMap<String, WebSocket> playerToConn = new ConcurrentHashMap<>();

    // gameId -> set of connections in that game
    private final ConcurrentMap<String, Set<WebSocket>> gameToConns = new ConcurrentHashMap<>();

    public void bindPlayer(WebSocket conn, String playerId) {
        if (conn == null || playerId == null || playerId.isBlank()) return;
        connToPlayer.put(conn, playerId);
        playerToConn.put(playerId, conn); // last connection wins
    }

    public void bindGame(WebSocket conn, String gameId) {
        if (conn == null || gameId == null || gameId.isBlank()) return;

        // remove from old game set if switching games
        String oldGame = connToGame.put(conn, gameId);
        if (oldGame != null && !oldGame.equals(gameId)) {
            Set<WebSocket> oldSet = gameToConns.get(oldGame);
            if (oldSet != null) oldSet.remove(conn);
        }

        gameToConns.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(conn);
    }

    public String getPlayerId(WebSocket conn) { return connToPlayer.get(conn); }
    public String getGameId(WebSocket conn)   { return connToGame.get(conn); }

    public boolean sendToPlayer(String playerId, String json) {
        WebSocket conn = playerToConn.get(playerId);
        if (conn == null || !conn.isOpen()) return false;
        conn.send(json);
        return true;
    }

    public int broadcastToGame(String gameId, String json) {
        Set<WebSocket> conns = gameToConns.get(gameId);
        if (conns == null) return 0;

        int sent = 0;
        for (WebSocket c : conns) {
            if (c != null && c.isOpen()) {
                c.send(json);
                sent++;
            }
        }
        return sent;
    }

    public int broadcastToGameExcept(String gameId, WebSocket exceptConn, String json) {
    Set<WebSocket> conns = gameToConns.get(gameId);
    if (conns == null) return 0;

    int sent = 0;
    for (WebSocket c : conns) {
        if (c == null) continue;
        if (c == exceptConn) continue;      // exclude sender
        if (!c.isOpen()) continue;
        c.send(json);
        sent++;
    }
    return sent;
    }

    public void removeConn(WebSocket conn) {
        if (conn == null) return;

        String playerId = connToPlayer.remove(conn);
        String gameId   = connToGame.remove(conn);

        if (playerId != null) {
            playerToConn.remove(playerId, conn); // only remove if still mapped to this conn
        }

        if (gameId != null) {
            Set<WebSocket> set = gameToConns.get(gameId);
            if (set != null) {
                set.remove(conn);
                if (set.isEmpty()) gameToConns.remove(gameId, set);
            }
        }
    }
}