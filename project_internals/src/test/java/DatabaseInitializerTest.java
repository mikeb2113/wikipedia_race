package com.example;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseInitializerTest {

    @BeforeAll
    static void setup() throws Exception {
        DatabaseInitializer.initialize();
    }

    @Test
    void playersTableExists() throws Exception {
        try (Connection conn = DatabaseInitializer.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "players", null)) {
                assertTrue(rs.next(), "players table should exist");
            }
        }
    }
}