package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import ui.util.SceneNavigator;

public class RulesController {

    @FXML
    private Button backButton;

    @FXML
    private void handleBack() {
        SceneNavigator.switchScene(
                backButton,
                "/ui/views/MainMenuView.fxml"
        );
    }
}
