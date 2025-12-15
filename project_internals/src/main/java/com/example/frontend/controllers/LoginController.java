package ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.example.frontend.AppContext;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;
@FXML
private void handleLogin() {

    String username = usernameField.getText().isBlank()
            ? "TestUser"
            : usernameField.getText().trim();

    try {
        if (AppContext.ws == null) {
            throw new IllegalStateException("WebSocket not ready: AppContext.ws is null");
        }

        AppContext.playerId = username;

        String reqId = "REQ_PING_" + System.currentTimeMillis();
        String pingJson =
                "{"
                        + "\"type\":\"PING\","
                        + "\"requestId\":\"" + reqId + "\","
                        + "\"playerId\":\"" + username + "\","
                        + "\"gameId\":null,"
                        + "\"payload\":{}"
                + "}";

        AppContext.ws.send(pingJson);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainMenuView.fxml"));
        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        MainMenuController controller = loader.getController();
        controller.setUsername(username);

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(scene);

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    /*@FXML
    private void handleLogin() {

        String fakeUsername = usernameField.getText().isBlank()
                ? "TestUser"
                : usernameField.getText();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/MainMenuView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    ClassLoader.class.getResource("/styles/app.css").toExternalForm()
            );

            MainMenuController controller = loader.getController();
            controller.setUsername(fakeUsername);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @FXML
    private void handleBack() {
        ((Stage) usernameField.getScene().getWindow()).close();
    }
}
