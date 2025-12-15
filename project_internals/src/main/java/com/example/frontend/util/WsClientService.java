package com.example.frontend.util;

import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.example.frontend.AppContext;
import java.net.URI;
import java.util.function.Consumer;

/**
 * Single WebSocket client for the JavaFX GUI.
 * Safe to use from controllers (UI updates always run on FX thread).
 */
public class WsClientService {

    private final WebSocketClient client;

    private Consumer<String> onMessage = msg -> {};
    private Consumer<Void> onOpen = v -> {};
    private Consumer<String> onClose = reason -> {};
    private Consumer<Exception> onError = ex -> {};

    public WsClientService(String wsUrl) {
        try {
            this.client = new WebSocketClient(new URI(wsUrl)) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    Platform.runLater(() -> onOpen.accept(null));
                }

               @Override
public void onMessage(String message) {
    Platform.runLater(() -> {
        handleProtocolMessage(message);
        onMessage.accept(message);
    });
}
private void handleProtocolMessage(String json) {

    // ---- CREATE_GAME response ----
    if (json.contains("\"type\":\"GAME_CREATED\"")) {

        String reqId = extractField(json, "requestId");

        if (reqId != null && reqId.equals(AppContext.pendingCreateReqId)) {

            String gameId = extractField(json, "gameId");

            System.out.println("[WS] GAME_CREATED received, gameId=" + gameId);

            AppContext.lastCreatedGameId = gameId;
        }
    }

    // Later:
    // if (json.contains("\"type\":\"GAME_STATE\"")) { ... }
    // if (json.contains("\"type\":\"PLAYER_JOINED\"")) { ... }
}
private static String extractField(String json, String field) {
    String key = "\"" + field + "\"";
    int i = json.indexOf(key);
    if (i < 0) return null;

    int colon = json.indexOf(':', i);
    int q1 = json.indexOf('"', colon + 1);
    int q2 = json.indexOf('"', q1 + 1);
    if (q1 < 0 || q2 < 0) return null;

    return json.substring(q1 + 1, q2);
}

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Platform.runLater(() -> onClose.accept(reason));
                }

                @Override
                public void onError(Exception ex) {
                    Platform.runLater(() -> onError.accept(ex));
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Invalid WS URL: " + wsUrl, e);
        }
    }

    /** Connect to the server */
    public void connect() {
        client.connect();
    }

    /** Send raw JSON (your WsEnvelope encoded as JSON) */
    public void send(String json) {
        if (!client.isOpen()) {
            throw new IllegalStateException("WebSocket not connected yet");
        }
        client.send(json);
    }

    public boolean isOpen() {
        return client.isOpen();
    }

    public void close() {
        client.close();
    }

    /* ---- Event hooks ---- */

    public void onMessage(Consumer<String> handler) {
        this.onMessage = handler;
    }

    public void onOpen(Runnable handler) {
        this.onOpen = v -> handler.run();
    }

    public void onClose(Consumer<String> handler) {
        this.onClose = handler;
    }

    public void onError(Consumer<Exception> handler) {
        this.onError = handler;
    }
}