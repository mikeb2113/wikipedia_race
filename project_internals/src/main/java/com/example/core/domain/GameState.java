package main.java.com.example.core.domain;

import java.util.List;

public class GameState {
    public final String gameId;
    public final String status;   // PENDING / STARTED / FINISHED
    public final Long startedAtMs; // nullable
    public final List<String> players;

    public GameState(String gameId, String status, Long startedAtMs, List<String> players) {
        this.gameId = gameId;
        this.status = status;
        this.startedAtMs = startedAtMs;
        this.players = players;
    }
}