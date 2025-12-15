package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.example.frontend.AppContext;
import ui.util.SceneNavigator;

public class LandingController {

    @FXML
    private Button howToPlayButton;

    @FXML
    private void handleLogin() {
        openModal("/views/LoginView.fxml", "Log In");
    }

    @FXML
    private void handleRegister() {
        openModal("/views/RegisterView.fxml", "Register");
    }

    private void openModal(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());
            /*scene.getStylesheets().add(
                    ClassLoader.class.getResource("/styles/app.css").toExternalForm()
            );*/

            Stage dialog = new Stage();
            dialog.setScene(scene);
            dialog.setTitle(title);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHowToPlay() {
        SceneNavigator.switchScene(
                howToPlayButton,
                "/views/RulesView.fxml"
        );
    }
}
