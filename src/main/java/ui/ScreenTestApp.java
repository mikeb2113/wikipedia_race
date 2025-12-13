package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ScreenTestApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/views/LandingView.fxml")
        );

        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(
                ClassLoader.getSystemResource("ui/styles/app.css").toExternalForm()
        );

        stage.setTitle("Wikipedia Race");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
