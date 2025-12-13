package com.example.websocket;

import com.example.websocket.GameWebSocketServer;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = 8080; // or read from env / args
        GameWebSocketServer server = new GameWebSocketServer(port);
        server.start();
        System.out.println("Wikipedia Race WebSocket server listening on ws://localhost:" + port);

        // Keep main thread alive
        Thread.currentThread().join();
    }
}