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
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() throws SQLException, IOException {
        // Ensure folder exists for the database file
        Files.createDirectories(Path.of("./data"));

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (Statement st = conn.createStatement()) {

                // ---- Load schema.sql from classpath ---- //
                InputStream in = DatabaseInitializer.class
                        .getClassLoader()
                        .getResourceAsStream("schema.sql");

                if (in == null) {
                    throw new IOException("schema.sql not found in classpath");
                }

                String ddl = new String(in.readAllBytes(), StandardCharsets.UTF_8);

                // Execute each SQL statement
                for (String sql : ddl.split(";")) {
                    sql = sql.trim();
                    if (sql.isEmpty()) continue;
                    st.execute(sql);
                }
            }

            conn.commit();
        }
    }
}