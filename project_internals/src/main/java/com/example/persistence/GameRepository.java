package com.example.persistence;

import main.java.com.example.core.domain.GameState;

import java.sql.Connection;

public interface GameRepository {
    long insertGame(Connection conn, long startArticleId, Long targetArticleId, Long createdByPlayerId, String state) throws Exception;
    boolean gameExists(Connection conn, long gameId)throws Exception;
    String getGameState(Connection conn, long gameId)throws Exception;
    void setGameStarted(Connection conn, long gameId)throws Exception; // sets start_time=now and state='STARTED'
    GameState loadGameState(Connection conn, long gameId)throws Exception;
    long getStartArticleId(Connection conn, long gameId) throws Exception;
    void tryFinishIfTargetReached(Connection conn,
                                  long gameId,
                                  long winnerId,
                                  long toArticleId) throws Exception;
}
