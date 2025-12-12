package com.example.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DbInspector {

    private static final String DB_PATH = "./data/wikirace.duckdb";
    private static final String JDBC_URL = "jdbc:duckdb:" + DB_PATH;

    public static void main(String[] args) {
        System.out.println("[DBI] DuckDB inspector starting...");
        System.out.println("[DBI] Target DB file: " + DB_PATH);

        try {
            if (!Files.exists(Path.of(DB_PATH))) {
                System.out.println("[DBI] Database file does not exist yet.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
                System.out.println("[DBI] Connected to DuckDB.");

                List<String> tables = listTables(conn);
                if (tables.isEmpty()) {
                    System.out.println("[DBI] No tables found in database.");
                    return;
                }

                System.out.println("[DBI] Found " + tables.size() + " table(s): " + tables);
                for (String table : tables) {
                    printTable(conn, table);
                }
            }

            System.out.println("[DBI] Done.");

        } catch (Exception e) {
            System.err.println("[DBI] Error while inspecting database:");
            e.printStackTrace();
        }
    }

    private static List<String> listTables(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();

        String sql =
            "SELECT table_name " +
            "FROM duckdb_tables " +
            "WHERE database_name = 'main' " +
            "  AND schema_name   = 'main' " +
            "ORDER BY table_name";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
        }

        return tables;
    }

    private static void printTable(Connection conn, String table) throws SQLException {
        System.out.println();
        System.out.println("=== TABLE: " + table + " ===");

        String query = "SELECT * FROM \"" + table + "\"";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            // Print headers
            for (int i = 1; i <= cols; i++) {
                System.out.print(meta.getColumnName(i));
                if (i < cols) System.out.print(" | ");
            }
            System.out.println();

            // Print separator
            for (int i = 1; i <= cols; i++) {
                System.out.print("---------");
                if (i < cols) System.out.print("+");
            }
            System.out.println();

            // Print rows
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                for (int i = 1; i <= cols; i++) {
                    String val = rs.getString(i);
                    System.out.print(val == null ? "NULL" : val);
                    if (i < cols) System.out.print(" | ");
                }
                System.out.println();
            }

            if (rowCount == 0) {
                System.out.println("(no rows)");
            }
        }
    }
}