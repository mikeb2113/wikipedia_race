package ui.controllers;

import com.example.frontend.AppContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import ui.util.SceneNavigator;

public class JoinLobbyController {

    @FXML private TextField lobbyCodeField;
    @FXML private Label statusLabel;

 @FXML
private void handleJoin() {
    String gameId = lobbyCodeField.getText().trim();

    if (gameId.isEmpty()) {
        statusLabel.setText("Please enter a lobby code.");
        return;
    }

    if (AppContext.ws == null) {
        statusLabel.setText("Not connected to server.");
        return;
    }

    String playerId = (AppContext.playerId != null && !AppContext.playerId.isBlank())
            ? AppContext.playerId
            : "TestUser";

    statusLabel.setText("Joining...");

    // save context immediately
    AppContext.currentGameId = gameId;
    AppContext.gameStarted = true;

    String reqId = "REQ_JOIN_" + System.currentTimeMillis();
    AppContext.pendingJoinReqId = reqId;

    String joinJson =
            "{"
                    + "\"type\":\"JOIN_GAME\","
                    + "\"requestId\":\"" + reqId + "\","
                    + "\"playerId\":\"" + escape(playerId) + "\","
                    + "\"gameId\":\"" + escape(gameId) + "\","
                    + "\"payload\":{}"
            + "}";

    // fire-and-forget join
    try {
        AppContext.ws.send(joinJson);
    } catch (Exception e) {
        // under deadline: still go to GameView, but show message
        e.printStackTrace();
        System.err.println("[GUI] JOIN send failed (continuing anyway): " + e.getMessage());
    }

    // fire-and-forget start (optional)
    String startReqId = "REQ_START_" + System.currentTimeMillis();
    String startJson =
            "{"
                    + "\"type\":\"START_GAME\","
                    + "\"requestId\":\"" + startReqId + "\","
                    + "\"playerId\":\"" + escape(playerId) + "\","
                    + "\"gameId\":\"" + escape(gameId) + "\","
                    + "\"payload\":{}"
            + "}";

    try {
        AppContext.ws.send(startJson);
    } catch (Exception startErr) {
        System.err.println("[GUI] START_GAME send failed (ignored): " + startErr.getMessage());
    }

    // ✅ GO TO GAME VIEW IMMEDIATELY (no waiting on GAME_STATE)
    SceneNavigator.switchScene(lobbyCodeField, "/views/GameView.fxml");
}

    @FXML
    private void handleBack() {
        SceneNavigator.switchScene(lobbyCodeField, "/views/MainMenuView.fxml");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}