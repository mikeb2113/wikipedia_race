package ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleRegister() {
        System.out.println("Register clicked");
        System.out.println("Username: " + usernameField.getText());
        System.out.println("Email: " + emailField.getText());
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
