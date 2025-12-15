package ui.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class SceneNavigator {

    private static final String STYLESHEET = "/styles/app.css";

    public static void switchScene(Node sourceNode, String fxmlPath) {
        try {
            URL fxmlUrl = SceneNavigator.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalStateException("FXML not found: " + fxmlPath);
            }

            Parent newRoot = FXMLLoader.load(fxmlUrl);

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene scene = stage.getScene();

            if (scene == null) {
                // First load only
                scene = new Scene(newRoot);
                stage.setScene(scene);
            } else {
                // ✅ THIS IS THE FIX
                scene.setRoot(newRoot);
            }

            // ✅ Add stylesheet ONCE
            URL cssUrl = SceneNavigator.class.getResource(STYLESHEET);
            if (cssUrl != null) {
                String css = cssUrl.toExternalForm();
                if (!scene.getStylesheets().contains(css)) {
                    scene.getStylesheets().add(css);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}