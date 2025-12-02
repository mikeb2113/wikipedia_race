package com.example;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();
            // then start websocket server / HTTP server / whatever
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
