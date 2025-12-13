package com.example.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VisitedArticlesRepositoryImpl implements VisitedArticlesRepository {

    private final Connection conn;

    public VisitedArticlesRepositoryImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insertIfAbsent(long gameId, long articleId) {
        String sql =
        "INSERT INTO visited_articles (game_id, article_id, first_seen_ts, first_seen_by_move) " +
        "VALUES (?, ?, NULL, NULL) " +
        "ON CONFLICT (game_id, article_id) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gameId);
            ps.setLong(2, articleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert visited_articles", e);
        }
    }
}
