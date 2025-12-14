package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {

        String fakeUsername = usernameField.getText().isBlank()
                ? "TestUser"
                : usernameField.getText();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/views/MainMenuView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    ClassLoader.getSystemResource("ui/styles/app.css").toExternalForm()
            );

            MainMenuController controller = loader.getController();
            controller.setUsername(fakeUsername);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        ((Stage) usernameField.getScene().getWindow()).close();
    }
}
