package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.example.frontend.AppContext;
import ui.util.SceneNavigator;
import com.example.frontend.AppContext;
import java.util.UUID;

public class CreateLobbyController {

    @FXML
    private TextField lobbyCodeField;

    @FXML
    private Label startArticleLabel;

    @FXML
    private Label targetArticleLabel;

    @FXML
    private Label hostLabel;

@FXML
private void initialize() {
    lobbyCodeField.setText("(creating...)");
    startArticleLabel.setText("United States");
    targetArticleLabel.setText("Albert Einstein");
    hostLabel.setText(AppContext.playerId != null ? AppContext.playerId : "TestUser");

    javafx.animation.Timeline t = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), e -> {
                if (AppContext.lastCreatedGameId != null && !AppContext.lastCreatedGameId.isBlank()) {
                    lobbyCodeField.setText(AppContext.lastCreatedGameId);
                }
            })
    );
    t.setCycleCount(javafx.animation.Animation.INDEFINITE);
    t.play();
}

    @FXML
    private void handleCopyCode() {
        System.out.println("Lobby code copied: " + lobbyCodeField.getText());
    }
private static String escape(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
}
@FXML
private void handleReady() {
    try {
        if (AppContext.ws == null) {
            System.err.println("[GUI] WS not ready");
            return;
        }

        String playerId = (AppContext.playerId != null && !AppContext.playerId.isBlank())
                ? AppContext.playerId
                : "TestUser";

        String startTitle = startArticleLabel.getText();   // or from a TextField
        String targetTitle = targetArticleLabel.getText();

        String reqId = "REQ_CREATE_" + System.currentTimeMillis();

        String json =
                "{"
                        + "\"type\":\"CREATE_GAME\","
                        + "\"requestId\":\"" + reqId + "\","
                        + "\"playerId\":\"" + playerId + "\","
                        + "\"gameId\":null,"
                        + "\"payload\":{"
                        +   "\"startTitle\":\"" + escape(startTitle) + "\","
                        +   "\"targetTitle\":\"" + escape(targetTitle) + "\""
                        + "}"
                + "}";

        // Send request
        AppContext.ws.send(json);

        // Now wait for the response in your WsClientService listener.
        // Deadline-simple: store reqId and handle in a global onMessage callback.
        AppContext.pendingCreateReqId = reqId;

        System.out.println("[GUI] Sent CREATE_GAME " + reqId);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    @FXML
    private void handleBack() {
        SceneNavigator.switchScene(
                lobbyCodeField,
                "/views/MainMenuView.fxml"
        );
    }

    private String generateLobbyCode() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();
    }
}
