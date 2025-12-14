package com.example.core.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ArticlesRepositoryImpl implements ArticlesRepository {

@Override
public long upsertArticle(Connection conn, long wikiPageId, String title, String canonicalTitle) throws Exception {

    // 1) exists?
    try (PreparedStatement ps = conn.prepareStatement(
            "SELECT article_id FROM articles WHERE article_id = ?"
    )) {
        ps.setLong(1, wikiPageId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        }
    }

    // 2) insert (MUST include title + canonical_title)
    try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO articles (article_id, title, canonical_title, last_updated) VALUES (?, ?, ?, CURRENT_TIMESTAMP)"
    )) {
        ps.setLong(1, wikiPageId);
        ps.setString(2, title);
        ps.setString(3, canonicalTitle);
        ps.executeUpdate();
        return wikiPageId;
    }
}

@Override
public String getTitleById(Connection conn, long articleId) throws Exception {
    try (PreparedStatement ps = conn.prepareStatement(
            "SELECT canonical_title FROM articles WHERE article_id = ?"
    )) {
        ps.setLong(1, articleId);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) throw new IllegalArgumentException("Unknown articleId: " + articleId);
            return rs.getString(1);
        }
    }
}
}