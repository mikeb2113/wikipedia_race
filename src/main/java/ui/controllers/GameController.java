package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import ui.util.SceneNavigator;


public class GameController {

    @FXML
    private Label articleTitleLabel;

    @FXML
    private Label clickCountLabel;

    @FXML
    private Label targetArticleLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private TextFlow articleTextFlow;

    @FXML
    private ListView<String> linksListView;

    private int clickCount = 0;

    @FXML
    private void initialize() {
        // NOTE: this is a dummy "article" view -- must be replaced with wikipedia integration
        // fake initial state
        articleTitleLabel.setText("United States");
        targetArticleLabel.setText("Target: Albert Einstein");
        timerLabel.setText("0:00");
        clickCountLabel.setText("0 clicks");

        articleTextFlow.getChildren().add(
                new Text("The United States of America is a country primarily located in North America...")
        );

        linksListView.getItems().addAll(
                "George Washington",
                "World War II",
                "Physics",
                "Germany",
                "Albert Einstein"
        );

        // fake clicking a link
        linksListView.setOnMouseClicked(event -> {
            String selected = linksListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleLinkClick(selected);
            }
        });
    }

    private void handleLinkClick(String article) {
        clickCount++;
        clickCountLabel.setText(clickCount + " clicks");

        articleTitleLabel.setText(article);

        articleTextFlow.getChildren().clear();
        articleTextFlow.getChildren().add(
                new Text("You navigated to the article: " + article)
        );
    }

    @FXML
    private void handleGiveUp() {
        SceneNavigator.switchScene(
                articleTitleLabel,
                "/ui/views/ResultsView.fxml"
        );
    }

}
