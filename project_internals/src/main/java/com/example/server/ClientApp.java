package com.example.frontend;

import com.example.frontend.util.WsClientService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        int port = readPortOrDefault();
        String wsUrl = "ws://localhost:" + port;

        System.out.println("[GUI] Connecting to " + wsUrl);

        // 🔌 Create ONE WebSocket connection for the whole app
        AppContext.ws = new WsClientService(wsUrl);

        AppContext.ws.onOpen(() ->
                System.out.println("[GUI] WebSocket OPEN")
        );

        AppContext.ws.onMessage(msg ->
                System.out.println("[GUI] WebSocket RX: " + msg)
        );

        AppContext.ws.onError(ex ->
                ex.printStackTrace()
        );

        AppContext.ws.connect();   // 🔑 connect here

        FXMLLoader loader = new FXMLLoader(
                ClientApp.class.getResource("/ui/views/LandingView.fxml")
        );

        Scene scene = new Scene(loader.load());
        stage.setTitle("Wikipedia Race (WS " + port + ")");
        stage.setScene(scene);
        stage.show();
    }

    private int readPortOrDefault() {
        try {
            return Integer.parseInt(Files.readString(Path.of(".wikirace_port")).trim());
        } catch (Exception e) {
            return 8080;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}