package com.example.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class VisitedArticlesRepositoryImpl implements VisitedArticlesRepository {

    public VisitedArticlesRepositoryImpl() {}

    @Override
    public void upsertVisited(Connection conn, long gameId, long articleId, long moveId) throws Exception {
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

    @Override
    public void insertIfAbsent(Connection conn, long gameId, long articleId) throws Exception {
        String sql =
            "INSERT INTO visited_articles (game_id, article_id, first_seen_ts, first_seen_by_move) " +
            "VALUES (?, ?, NULL, NULL) " +
            "ON CONFLICT (game_id, article_id) DO NOTHING";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gameId);
            ps.setLong(2, articleId);
            ps.executeUpdate();
        }
    }
}