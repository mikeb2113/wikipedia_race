package com.example.core.domain;

import java.sql.Connection;

public interface ArticlesRepository {

    long upsertArticle(
        Connection conn,
        long wikiPageId,
        String title,
        String canonicalTitle
    ) throws Exception;

    String getTitleById(Connection conn, long articleId) throws Exception;
}
