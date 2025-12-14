package ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.controllers.ErrorDialogController;

public class ErrorDialogUtil {

    public static void showError(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ErrorDialogUtil.class.getResource("/ui/views/ErrorDialog.fxml")
            );

            Scene scene = new Scene(loader.load());

            ErrorDialogController controller = loader.getController();
            controller.setErrorMessage(message);

            Stage stage = new Stage();
            stage.setTitle("Error");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
