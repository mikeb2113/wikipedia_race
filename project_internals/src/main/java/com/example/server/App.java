package com.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import com.example.persistence.DatabaseInitializer;
import com.example.websocket.ServerMain;
//import com.example.Docker_setup;

public class App {
    public static void main(String[] args) {
        try {
            System.out.println("[APP] Initializing database...");
            DatabaseInitializer.initialize();
            ServerMain.main(args);
            System.out.println("[APP] Database initialization complete.");

            // Placeholder for future server start
            System.out.println("[APP] Ready for next steps (server, API, etc.)");

        } catch (Exception e) {
            System.err.println("[APP] Fatal error:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
    /*private static void docker_run(){
    try {
            String[] cmd = {"docker", "run", "--rm", "-p","8080:8080", "docker-java-server"};
            System.out.println("Attempting to Run Docker...");
            Process proc = new ProcessBuilder(cmd)
                    .inheritIO() // optional: pipe output to console
                    .start();

            proc.waitFor();  // wait for docker to finish

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }*/
