package main.java.com.example.core.domain;
import main.java.com.example.core.domain.GameState;
public class CreateGameResult {
    public final String gameId;
    public final GameState state;

    public CreateGameResult(String gameId, GameState state) {
        this.gameId = gameId;
        this.state = state;
    }
}
