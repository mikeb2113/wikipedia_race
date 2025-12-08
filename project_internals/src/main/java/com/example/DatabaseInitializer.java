package com.example;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private static final String DB_FILE = "./data/wikirace.duckdb";
    private static final String DB_URL = "jdbc:duckdb:" + DB_FILE;

    private DatabaseInitializer() {}

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:duckdb:./data/wikirace.duckdb";
        System.out.println("[DB] Opening DuckDB connection at: " + url);
        return DriverManager.getConnection(url);
    }

    public static void initialize() throws SQLException, IOException {
        System.out.println("[DB] Ensuring data directory exists...");
        Files.createDirectories(Path.of("./data"));

        try (Connection conn = getConnection()) {

            System.out.println("[DB] Connection established.");
            conn.setAutoCommit(false);

            try (Statement st = conn.createStatement()) {

                System.out.println("[DB] Loading schema.sql from classpath...");
                InputStream in = DatabaseInitializer.class
                        .getClassLoader()
                        .getResourceAsStream("schema.sql");

                if (in == null) {
                    throw new IOException("[DB] ERROR: schema.sql not found in classpath");
                }

                String ddl = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("[DB] Schema read. Length = " + ddl.length() + " chars");

                String[] statements = ddl.split(";");
                System.out.println("[DB] Found " + statements.length + " SQL statements.");

                int counter = 1;
                for (String sql : statements) {
                    sql = sql.trim();
                    if (sql.isEmpty()) continue;

                    System.out.println("[DB] Executing SQL statement " + counter + "...");
                    // Optional: print SQL for debugging
                    // System.out.println(sql);
                    
                    st.execute(sql);
                    counter++;
                }
            }

            System.out.println("[DB] Committing changes...");
            conn.commit();
            System.out.println("[DB] Schema successfully applied.");
        }
    }

}