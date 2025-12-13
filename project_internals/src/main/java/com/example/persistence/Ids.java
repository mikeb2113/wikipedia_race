package com.example.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class Ids {
    private Ids() {}

    public static long parseLongOrLookupPlayerId(Connection conn, String playerIdOrUsername) throws Exception {
        if (playerIdOrUsername == null || playerIdOrUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing playerId");
        }
        String s = playerIdOrUsername.trim();
        if (s.matches("\\d+")) return Long.parseLong(s);

        // treat as username
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT player_id FROM players WHERE username = ?"
        )) {
            ps.setString(1, s);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalArgumentException("Unknown username: " + s);
    }

    public static long parseLongId(String id, String fieldName) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Missing " + fieldName);
        String s = id.trim();
        if (!s.matches("\\d+")) throw new IllegalArgumentException(fieldName + " must be numeric for now: " + s);
        return Long.parseLong(s);
    }
}