package com.example.persistence;

import main.java.com.example.core.domain.GameState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DuckDbGameRepository implements GameRepository {

    private final MembershipRepository membershipRepo;

    public DuckDbGameRepository(MembershipRepository membershipRepo) {
        this.membershipRepo = membershipRepo;
    }

    @Override
    public long insertGame(Connection conn, long startArticleId, Long targetArticleId,
                           Long createdByPlayerId, String state) throws Exception {

        // start_time is NOT NULL, so we set it now; startGame can update it again if you want.
        String sql = "INSERT INTO games (start_time, start_article_id, target_article_id, created_by, state) " +
                     "VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?) RETURNING game_id";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, startArticleId);

            if (targetArticleId == null) ps.setNull(2, java.sql.Types.BIGINT);
            else ps.setLong(2, targetArticleId);

            if (createdByPlayerId == null) ps.setNull(3, java.sql.Types.BIGINT);
            else ps.setLong(3, createdByPlayerId);

            ps.setString(4, state != null ? state : "PENDING");

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Insert game failed (no game_id returned)");
                return rs.getLong(1);
            }
        }
    }

    @Override
    public boolean gameExists(Connection conn, long gameId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM games WHERE game_id = ?"
        )) {
            ps.setLong(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public String getGameState(Connection conn, long gameId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT state FROM games WHERE game_id = ?"
        )) {
            ps.setLong(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Game not found: " + gameId);
                return rs.getString(1);
            }
        }
    }

    @Override
    public void setGameStarted(Connection conn, long gameId) throws Exception {
        // Also sets start_time to NOW per your “START_GAME writes start timestamp + state change”
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE games SET state = 'STARTED', start_time = CURRENT_TIMESTAMP " +
                "WHERE game_id = ? AND state <> 'FINISHED'"
        )) {
            ps.setLong(1, gameId);
            ps.executeUpdate();
        }
    }

    @Override
    public GameState loadGameState(Connection conn, long gameId) throws Exception {
        String state;
        Timestamp startTime;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT state, start_time FROM games WHERE game_id = ?"
        )) {
            ps.setLong(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Game not found: " + gameId);
                state = rs.getString(1);
                startTime = rs.getTimestamp(2);
            }
        }

        List<String> players = membershipRepo.listUsernames(conn, gameId);
        Long startedAtMs = (startTime != null ? startTime.getTime() : null);

        return new GameState(Long.toString(gameId), state, startedAtMs, players);
    }
    @Override
    public long getStartArticleId(Connection conn, long gameId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT start_article_id FROM games WHERE game_id = ?"
        )) {
            ps.setLong(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Game not found: " + gameId);
                return rs.getLong(1);
            }
        }
    }
    @Override
public void tryFinishIfTargetReached(Connection conn, long gameId, long winnerId, long toArticleId) throws Exception {
    try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE games SET state='FINISHED', end_time=CURRENT_TIMESTAMP, winner_id=? " +
            "WHERE game_id=? AND target_article_id=?"
    )) {
        ps.setLong(1, winnerId);
        ps.setLong(2, gameId);
        ps.setLong(3, toArticleId);
        ps.executeUpdate();
    }
}
}