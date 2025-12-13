package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import ui.util.SceneNavigator;

public class ResultsController {

    @FXML
    private Label clicksLabel;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private void initialize() {
        // default values for UI testing; overwritten when setResults() is called
        // or intitalize() can be removed entirely when backend is done
        timeLabel.setText("3:47");
        clicksLabel.setText("7");
        scoreLabel.setText("+2");
    }

    public void setResults(String time, int clicks, int score) {
        timeLabel.setText(time);
        clicksLabel.setText(String.valueOf(clicks));
        scoreLabel.setText(score >= 0 ? "+" + score : String.valueOf(score));
    }

    @FXML
    private void handlePlayAgain() {
        SceneNavigator.switchScene(
                timeLabel,
                "/ui/views/GameView.fxml"
        );
    }

    @FXML
    private void handleExitToMenu() {
        SceneNavigator.switchScene(
                timeLabel,
                "/ui/views/MainMenuView.fxml"
        );
    }
}
