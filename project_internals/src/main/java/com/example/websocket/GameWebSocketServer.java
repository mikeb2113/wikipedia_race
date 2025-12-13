package com.example.websocket;

/*import com.example.core.GameService;
import com.example.core.InMemoryGameService;*/
import com.example.websocket.WebSocketMessageHandler;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class GameWebSocketServer extends WebSocketServer {

    private final WebSocketMessageHandler handler;
    private final ConnectionRegistry registry = new ConnectionRegistry();

    public GameWebSocketServer(int port) {
        super(new InetSocketAddress(port));

        /*GameService gameService = new InMemoryGameService();*/ // your core logic impl
        this.handler = new WebSocketMessageHandler();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("WebSocket opened: " + conn.getRemoteSocketAddress());
        // nothing to bind yet; we learn player/game from messages
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            // Let handler process, but ALSO allow it to broadcast via registry
            String responseJson = handler.handleIncoming(message, conn, registry);

            if (responseJson != null) {
                conn.send(responseJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
            conn.send("{\"type\":\"ERROR\",\"code\":\"INTERNAL_ERROR\",\"message\":\"Unexpected server error.\"}");
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + reason + " from " + conn.getRemoteSocketAddress());
        registry.removeConn(conn); // ✅ cleanup
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on " + getAddress());
    }
}