package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import ui.util.SceneNavigator;

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
        lobbyCodeField.setText(generateLobbyCode());
        startArticleLabel.setText("United States");
        targetArticleLabel.setText("Albert Einstein");
        hostLabel.setText("TestUser");
    }

    @FXML
    private void handleCopyCode() {
        System.out.println("Lobby code copied: " + lobbyCodeField.getText());
    }

    @FXML
    private void handleReady() {
        System.out.println("Host is ready");
    }

    @FXML
    private void handleBack() {
        SceneNavigator.switchScene(
                lobbyCodeField,
                "/ui/views/MainMenuView.fxml"
        );
    }

    private String generateLobbyCode() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();
    }
}
