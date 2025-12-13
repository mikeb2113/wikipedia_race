package com.example.persistence;

import java.sql.Connection;
import java.sql.SQLException;

public class TxRunner {

    public interface TxWork<T> {
        T run(Connection conn) throws Exception;
    }

    private final ConnectionProvider connectionProvider;

    public TxRunner(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public <T> T inTransaction(TxWork<T> work) {
        try (Connection conn = connectionProvider.getConnection()) {
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                T result = work.run(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw new RuntimeException(e);
            } finally {
                try { conn.setAutoCommit(oldAuto); } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}