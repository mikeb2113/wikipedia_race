package com.example.persistence;

import java.sql.Connection;
import java.util.List;

public interface MembershipRepository {
    void addPlayer(Connection conn, long gameId, long playerId) throws Exception;     // INSERT plays (idempotent via PK)
    boolean isMember(Connection conn, long gameId, long playerId) throws Exception;
    List<String> listUsernames(Connection conn, long gameId) throws Exception;
        void incrementStepsTaken(Connection conn, long gameId, long playerId) throws Exception;
}