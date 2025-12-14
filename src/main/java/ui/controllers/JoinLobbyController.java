package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import ui.util.SceneNavigator;

public class JoinLobbyController {

    @FXML
    private TextField lobbyCodeField;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleJoin() {
        String code = lobbyCodeField.getText().trim();

        if (code.isEmpty()) {
            statusLabel.setText("Please enter a lobby code.");
            return;
        }

        System.out.println("Joining lobby: " + code);
        statusLabel.setText("");

        SceneNavigator.switchScene(
                lobbyCodeField,
                "/ui/views/LobbyView.fxml"
        );
    }

    @FXML
    private void handleBack() {
        SceneNavigator.switchScene(
                lobbyCodeField,
                "/ui/views/MainMenuView.fxml"
        );
    }
}
