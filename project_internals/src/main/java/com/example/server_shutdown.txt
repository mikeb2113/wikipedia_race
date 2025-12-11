package com.example;

import java.io.IOException;

import com.example.App;

public class server_shutdown {
    public static void main(String[] args){
        try {
        String[] cmd = {"docker", "stop", "wikirace_server"};
        System.out.println("Stopping Docker server...");
        Process proc = new ProcessBuilder(cmd)
                .inheritIO()
                .start();

        proc.waitFor();
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
    }
}
