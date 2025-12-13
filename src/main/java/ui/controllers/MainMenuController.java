package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import ui.util.SceneNavigator;

public class MainMenuController {

    @FXML
    private Label welcomeLabel;

    public void setUsername(String username) {
        welcomeLabel.setText("Welcome " + username + "!");
    }

    @FXML
    private void handleCreateLobby() {
        SceneNavigator.switchScene(
                welcomeLabel,
                "/ui/views/CreateLobbyView.fxml"
        );
    }

    @FXML
    private void handleJoinLobby() {
        SceneNavigator.switchScene(
                welcomeLabel,
                "/ui/views/JoinLobbyView.fxml"
        );
    }

    @FXML
    private void handleHowToPlay() {
        SceneNavigator.switchScene(
                welcomeLabel,
                "/ui/views/RulesView.fxml"
        );
    }

    @FXML
    private void handleLogout() {
        SceneNavigator.switchScene(
                welcomeLabel,
                "/ui/views/LandingView.fxml"
        );
    }
}
