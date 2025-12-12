package com.example.persistence;

public interface VisitedArticlesRepository {
    void insertIfAbsent(long gameId, long articleId);
}