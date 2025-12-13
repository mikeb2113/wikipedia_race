package ui.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {

    private static final String STYLESHEET =
            "ui/styles/app.css";

    public static void switchScene(Node sourceNode, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource(fxmlPath)
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    ClassLoader.getSystemResource(STYLESHEET).toExternalForm()
            );

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
