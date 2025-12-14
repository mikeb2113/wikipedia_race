package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import ui.util.SceneNavigator;

public class LobbyController {

    @FXML private Label lobbyCodeLabel;
    @FXML private Label startArticleLabel;
    @FXML private Label targetArticleLabel;
    @FXML private Label player1Label;
    @FXML private Label player1Status;
    @FXML private Label player2Label;
    @FXML private Label player2Status;

    private boolean isReady = false;

    @FXML
    private void initialize() {
        lobbyCodeLabel.setText("Lobby Code: ABC123");
        startArticleLabel.setText("United States");
        targetArticleLabel.setText("Albert Einstein");

        player1Label.setText("TestUser");
        player1Status.setText("Not Ready");

        player2Label.setText("Waiting...");
        player2Status.setText("Not Ready");
    }

    @FXML
    private void handleReady() {
        isReady = !isReady;
        player1Status.setText(isReady ? "Ready" : "Not Ready");
    }

    @FXML
    private void handleLeaveLobby() {
        SceneNavigator.switchScene(
                player1Label,
                "/ui/views/MainMenuView.fxml"
        );
    }
}
