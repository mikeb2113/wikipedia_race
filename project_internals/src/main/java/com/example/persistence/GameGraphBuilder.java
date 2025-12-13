package com.example.persistence;

import java.util.*;

public class GameGraphBuilder {

    private final LinksRepository linksRepo;
    private final VisitedArticlesRepository visitedRepo;

    public GameGraphBuilder(LinksRepository linksRepo,
                            VisitedArticlesRepository visitedRepo) {
        this.linksRepo = linksRepo;
        this.visitedRepo = visitedRepo;
    }

    /**
     * Build a BFS "tree" of reachable articles for a game, starting
     * from startArticleId, and populate visited_articles.
     */
    public void buildInitialGraphForGame(
            long gameId,
            long startArticleId,
            int maxDepth,
            int maxNodes
    ) {
        Queue<Long> queue = new ArrayDeque<>();
        Map<Long, Integer> depth = new HashMap<>();

        queue.add(startArticleId);
        depth.put(startArticleId, 0);

        int nodesVisited = 0;

        while (!queue.isEmpty()) {
            long current = queue.remove();
            int d = depth.get(current);

            // Record that this article is part of this game's world
            visitedRepo.insertIfAbsent(gameId, current);

            nodesVisited++;
            if (nodesVisited >= maxNodes) break;

            if (d >= maxDepth) {
                continue; // don't expand further
            }

            // Get outgoing neighbors from links table (populate links if needed)
            List<Long> neighbors = linksRepo.getNeighbors(current);

            for (long neighbor : neighbors) {
                if (!depth.containsKey(neighbor)) {
                    depth.put(neighbor, d + 1);
                    queue.add(neighbor);
                }
            }
        }
    }
}