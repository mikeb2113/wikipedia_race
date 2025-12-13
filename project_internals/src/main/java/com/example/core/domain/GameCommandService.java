package main.java.com.example.core.domain;
public interface GameCommandService {
    CreateGameResult createGame(String playerId /*, GameConfig cfg */);
    GameState joinGame(String gameId, String playerId);
    GameState startGame(String gameId, String playerId);
    MoveResult applyMove(String gameId, String playerId, long fromId, long toId);
}