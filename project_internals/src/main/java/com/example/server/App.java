package com.example.server;

import com.example.persistence.DatabaseInitializer;
import com.example.websocket.ServerMain;

import java.nio.file.Files;
import java.nio.file.Path;

public class App {
    public static void main(String[] args) {
        try {
            // 🔎 Show working directory
            System.out.println("[APP] CWD = " + System.getProperty("user.dir"));

            // 🔎 Show expected DB paths
            System.out.println("[APP] Expected data dir = " + Path.of("./data").toAbsolutePath());
            System.out.println("[APP] Expected db file = " + Path.of("./data/wikirace.duckdb").toAbsolutePath());

            System.out.println("[APP] Initializing database...");
            DatabaseInitializer.initialize();

            // 🔎 Check if DB file exists after init
            System.out.println(
                "[APP] DB file exists after init? " +
                Files.exists(Path.of("./data/wikirace.duckdb"))
            );

            // Start server
            ServerMain.main(args);

            System.out.println("[APP] Database initialization complete.");
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
