package com.example;

public class GameState {
    private long gameId;
    private GameStatus status;
    private long startArticleId;
    private long endArticleId;

    public GameState(long gameId, GameStatus status, long startArticleId, long endArticleId) {
        this.gameId = gameId;
        this.status = status;
        this.startArticleId = startArticleId;
        this.endArticleId = endArticleId;
    }

    // getters only (no logic)
    public long getGameId() { return gameId; }
    public GameStatus getStatus() { return status; }
    public long getStartArticleId() { return startArticleId; }
    public long getEndArticleId() { return endArticleId; }
}