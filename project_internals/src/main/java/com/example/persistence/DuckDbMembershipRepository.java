package com.example.persistence;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public class DuckDbMembershipRepository implements MembershipRepository {

    @Override
    public void addPlayer(Connection conn, long gameId, long playerId) throws Exception {
        // plays has PK(player_id, game_id) so this becomes naturally idempotent
        // DuckDB supports ON CONFLICT DO NOTHING
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO plays (player_id, game_id, joined_at) VALUES (?, ?, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (player_id, game_id) DO NOTHING"
        )) {
            ps.setLong(1, playerId);
            ps.setLong(2, gameId);
            ps.executeUpdate();
        }
    }
@Override
public void incrementStepsTaken(Connection conn, long gameId, long playerId) throws Exception {
    try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE plays SET steps_taken = steps_taken + 1 " +
            "WHERE game_id = ? AND player_id = ? AND left_at IS NULL"
    )) {
        ps.setLong(1, gameId);
        ps.setLong(2, playerId);
        ps.executeUpdate();
    }
}
    @Override
    public boolean isMember(Connection conn, long gameId, long playerId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM plays WHERE game_id = ? AND player_id = ? AND left_at IS NULL"
        )) {
            ps.setLong(1, gameId);
            ps.setLong(2, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<String> listUsernames(Connection conn, long gameId) throws Exception {
        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT p.username " +
                "FROM plays pl " +
                "JOIN players p ON p.player_id = pl.player_id " +
                "WHERE pl.game_id = ? AND pl.left_at IS NULL " +
                "ORDER BY pl.joined_at ASC"
        )) {
            ps.setLong(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        }
        return out;
    }
}