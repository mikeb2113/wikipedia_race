package com.example;


public class App {
    public static void main(String[] args) {
        System.out.println("[APP] Starting application...");

        try {
            System.out.println("[APP] Initializing database...");
            DatabaseInitializer.initialize();
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
