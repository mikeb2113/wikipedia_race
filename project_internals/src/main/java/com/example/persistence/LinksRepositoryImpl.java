package com.example.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LinksRepositoryImpl implements LinksRepository {

    private final Connection conn;

    public LinksRepositoryImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<Long> getNeighbors(long fromArticleId) {
        String sql = "SELECT to_article_id FROM links WHERE from_article_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, fromArticleId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Long> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rs.getLong(1));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load neighbors for article " + fromArticleId, e);
        }
    }
}