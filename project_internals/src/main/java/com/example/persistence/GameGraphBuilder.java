package com.example.persistence;

import java.sql.Connection;
import java.util.*;

public class GameGraphBuilder {

    private final LinksRepository linksRepo;
    private final VisitedArticlesRepository visitedRepo;

    public GameGraphBuilder(LinksRepository linksRepo,
                            VisitedArticlesRepository visitedRepo) {
        this.linksRepo = linksRepo;
        this.visitedRepo = visitedRepo;
    }

    public void buildInitialGraphForGame(
            Connection conn,
            long gameId,
            long startArticleId,
            int maxDepth,
            int maxNodes
    ) throws Exception{
        Queue<Long> queue = new ArrayDeque<>();
        Map<Long, Integer> depth = new HashMap<>();

        queue.add(startArticleId);
        depth.put(startArticleId, 0);

        int nodesVisited = 0;

        while (!queue.isEmpty()) {
            long current = queue.remove();
            int d = depth.get(current);

            // Record that this article is part of this game's world
            visitedRepo.insertIfAbsent(conn, gameId, current);

            nodesVisited++;
            if (nodesVisited >= maxNodes) break;

            if (d >= maxDepth) continue;

            List<Long> neighbors = linksRepo.getNeighbors(conn, current);
            for (long neighbor : neighbors) {
                if (!depth.containsKey(neighbor)) {
                    depth.put(neighbor, d + 1);
                    queue.add(neighbor);
                }
            }
        }
    }
}