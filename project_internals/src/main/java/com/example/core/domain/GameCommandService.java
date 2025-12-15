package com.example.core.domain;
public interface GameCommandService {
    CreateGameResult createGame(String playerId, String startTitle, String targetTitle);    
    GameState joinGame(String gameId, String playerId);
    GameState startGame(String gameId, String playerId);
    MoveResult applyMove(String gameId, String playerId, long fromId, long toId);
    GameCommandServiceImpl buildGameService();

}