package com.example.persistence.DTOs;

public class MoveResultDTO {
    public String gameId;
    public String playerId;
    public boolean valid;
    public boolean isWinningMove;
    public String reason; // null if valid
}