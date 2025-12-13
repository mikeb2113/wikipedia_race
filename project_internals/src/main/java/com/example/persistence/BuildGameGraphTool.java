package com.example.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BuildGameGraphTool {

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.err.println("Usage: BuildGameGraphTool <gameId> <startArticleId> [maxDepth] [maxNodes]");
            System.exit(1);
        }

        long gameId = Long.parseLong(args[0]);
        long startArticleId = Long.parseLong(args[1]);
        int maxDepth = (args.length >= 3) ? Integer.parseInt(args[2]) : 3;
        int maxNodes = (args.length >= 4) ? Integer.parseInt(args[3]) : 500;

        // Make sure schema is applied
        DatabaseInitializer.initialize();

        try (Connection conn = DatabaseInitializer.getConnection()) {

            ensureGameExists(conn, gameId);
            ensureArticleExists(conn, startArticleId);

            LinksRepository linksRepo = new LinksRepositoryImpl();
            VisitedArticlesRepository visitedRepo = new VisitedArticlesRepositoryImpl();
            GameGraphBuilder builder = new GameGraphBuilder(linksRepo, visitedRepo);

            System.out.printf(
                "Building graph for game %d from article %d (maxDepth=%d, maxNodes=%d)%n",
                gameId, startArticleId, maxDepth, maxNodes
            );

            builder.buildInitialGraphForGame(conn,gameId, startArticleId, maxDepth, maxNodes);
            printVisitedArticles(conn, gameId);
        }
    }

    private static void ensureGameExists(Connection conn, long gameId) throws SQLException {
        String sql = "SELECT game_id FROM games WHERE game_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException(
                        "No game with game_id=" + gameId +
                        ". Insert a real game row into 'games' first.");
                }
            }
        }
    }

    private static void ensureArticleExists(Connection conn, long articleId) throws SQLException {
        String sql = "SELECT article_id, title FROM articles WHERE article_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException(
                        "No article with article_id=" + articleId +
                        ". Insert a row into 'articles' first (real or stub).");
                } else {
                    System.out.println("[OK] Start article: " +
                        rs.getLong("article_id") + " - " + rs.getString("title"));
                }
            }
        }
    }

    private static void printVisitedArticles(Connection conn, long gameId) throws SQLException {
        String sql =
            "SELECT v.article_id, a.title " +
            "FROM visited_articles v " +
            "JOIN articles a ON v.article_id = a.article_id " +
            "WHERE v.game_id = ? " +
            "ORDER BY v.article_id";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Visited articles for game " + gameId + ":");
                while (rs.next()) {
                    long articleId = rs.getLong("article_id");
                    String title = rs.getString("title");
                    System.out.printf("  - [%d] %s%n", articleId, title);
                }
            }
        }
    }
}