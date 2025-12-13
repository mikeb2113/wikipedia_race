package com.example.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DuckDbMoveRepository implements MoveRepository {

    @Override
    public int nextMoveSeq(Connection conn, long gameId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(MAX(move_seq), 0) + 1 FROM moves WHERE game_id = ?"
        )) {
            ps.setLong(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public long insertMove(Connection conn, long gameId, long playerId, int moveSeq,
                           long fromArticleId, long toArticleId, String moveStatus) throws Exception {
        String sql =
                "INSERT INTO moves (move_seq, move_status, from_article_id, to_article_id, game_id, player_id) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING move_id";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, moveSeq);
            ps.setString(2, moveStatus != null ? moveStatus : "OK");
            ps.setLong(3, fromArticleId);
            ps.setLong(4, toArticleId);
            ps.setLong(5, gameId);
            ps.setLong(6, playerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Insert move failed (no move_id returned)");
                return rs.getLong(1);
            }
        }
    }
    @Override
    public Long findLastToArticle(Connection conn, long gameId, long playerId) throws Exception {
        String sql =
            "SELECT to_article_id FROM moves " +
            "WHERE game_id = ? AND player_id = ? " +
            "ORDER BY move_seq DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gameId);
            ps.setLong(2, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }
private long deriveCurrentArticle(Connection conn, long gameId, long playerId) throws Exception {
    String sql =
        "SELECT COALESCE(" +
        "  (SELECT m.to_article_id FROM moves m WHERE m.game_id = ? AND m.player_id = ? ORDER BY m.move_seq DESC LIMIT 1)," +
        "  (SELECT g.start_article_id FROM games g WHERE g.game_id = ?)" +
        ")";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, gameId);
        ps.setLong(2, playerId);
        ps.setLong(3, gameId);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) throw new IllegalStateException("Could not derive current article");
            return rs.getLong(1);
        }
    }
}

private boolean linkExists(Connection conn, long fromId, long toId) throws Exception {
    try (PreparedStatement ps = conn.prepareStatement(
            "SELECT 1 FROM links WHERE from_article_id = ? AND to_article_id = ?"
    )) {
        ps.setLong(1, fromId);
        ps.setLong(2, toId);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }
}

private void incrementStepsTaken(Connection conn, long gameId, long playerId) throws Exception {
    try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE plays SET steps_taken = steps_taken + 1 WHERE game_id = ? AND player_id = ? AND left_at IS NULL"
    )) {
        ps.setLong(1, gameId);
        ps.setLong(2, playerId);
        ps.executeUpdate();
    }
}

private void upsertVisited(Connection conn, long gameId, long articleId, long moveId) throws Exception {
    try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO visited_articles (game_id, article_id, first_seen_ts, first_seen_by_move) " +
            "VALUES (?, ?, CURRENT_TIMESTAMP, ?) " +
            "ON CONFLICT (game_id, article_id) DO NOTHING"
    )) {
        ps.setLong(1, gameId);
        ps.setLong(2, articleId);
        ps.setLong(3, moveId);
        ps.executeUpdate();
    }
}
}