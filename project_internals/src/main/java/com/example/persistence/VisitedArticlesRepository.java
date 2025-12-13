package com.example.persistence;

import java.sql.Connection;

public interface VisitedArticlesRepository {
    void upsertVisited(Connection conn, long gameId, long articleId, long moveId) throws Exception;
    void insertIfAbsent(Connection conn, long gameId, long articleId) throws Exception;
}