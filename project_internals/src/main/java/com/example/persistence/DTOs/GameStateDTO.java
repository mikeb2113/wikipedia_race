package com.example.persistence.DTOs;
import java.util.List;

public class GameStateDTO {
    public String gameId;
    public String ownerId;
    public String phase;  // LOBBY, IN_PROGRESS, FINISHED

    public long startArticleId;
    public long endArticleId;

    public List<PlayerDTO> players;
    public String currentTurnPlayerId;
}