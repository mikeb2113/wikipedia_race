import java.io.IOException;

import com.example.DatabaseInitializer;

public class entry_point {

    public static void main(String[] args){
        docker_build();
        docker_run();
        try {
            DatabaseInitializer.initialize();
            System.out.println("Database initialized!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void docker_build(){
    try {
            String[] cmd = {"docker", "build", "-t", "docker-java-server", "."};

            Process proc = new ProcessBuilder(cmd)
                    .inheritIO() // optional: pipe output to console
                    .start();

            proc.waitFor();  // wait for docker to finish

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void docker_run(){
    try {
            String[] cmd = {"docker", "run", "--rm", "docker-java-server"};

            Process proc = new ProcessBuilder(cmd)
                    .inheritIO() // optional: pipe output to console
                    .start();

            proc.waitFor();  // wait for docker to finish

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/*
Error: Could not find or load main class com.example.DatabaseInitializer
Caused by: java.lang.ClassNotFoundException: com.example.DatabaseInitializer
java.sql.SQLException: No suitable driver found for jdbc:duckdb:./data/wikirace.duckdb
        at java.sql/java.sql.DriverManager.getConnection(DriverManager.java:706)
        at java.sql/java.sql.DriverManager.getConnection(DriverManager.java:252)
        at com.example.DatabaseInitializer.getConnection(DatabaseInitializer.java:20)
        at com.example.DatabaseInitializer.initialize(DatabaseInitializer.java:27)
        at entry_point.main(entry_point.java:11)
 */