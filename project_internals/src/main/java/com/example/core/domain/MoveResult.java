package main.java.com.example.core.domain;
import main.java.com.example.core.domain.GameState;

public class MoveResult {
    public final String gameId;
    public final String playerId;
    public final long fromArticleId;
    public final long toArticleId;
    public final GameState state; // updated state snapshot (or null if you prefer)

    public MoveResult(String gameId, String playerId, long fromArticleId, long toArticleId, GameState state) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.fromArticleId = fromArticleId;
        this.toArticleId = toArticleId;
        this.state = state;
    }
}