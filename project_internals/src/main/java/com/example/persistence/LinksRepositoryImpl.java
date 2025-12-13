package com.example.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LinksRepositoryImpl implements LinksRepository {

    public LinksRepositoryImpl() {} // optional

    @Override
    public List<Long> getNeighbors(Connection conn, long fromArticleId) throws Exception {
        String sql = "SELECT to_article_id FROM links WHERE from_article_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, fromArticleId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Long> out = new ArrayList<>();
                while (rs.next()) out.add(rs.getLong(1));
                return out;
            }
        }
    }

    @Override
    public boolean linkExists(Connection conn, long fromId, long toId) throws Exception {
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
}