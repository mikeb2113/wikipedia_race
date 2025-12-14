package com.example.core.domain;

import wiki.WikipediaService;

import com.example.persistence.LinksRepository;
import com.example.persistence.VisitedArticlesRepository;
import com.example.persistence.Ids;
import com.example.persistence.GameRepository;
import com.example.persistence.MembershipRepository;
import com.example.persistence.MoveRepository;
import com.example.persistence.TxRunner;
import com.example.core.domain.ArticlesRepository;

import java.util.Objects;

public class GameCommandServiceImpl implements GameCommandService {

    private final WikipediaService wikipedia;

    private final TxRunner tx;
    private final GameRepository games;
    private final MembershipRepository members;
    private final MoveRepository moves;
    private final LinksRepository links;
    private final VisitedArticlesRepository visited;
    private final ArticlesRepository articles; // ✅ field exists now

    public GameCommandServiceImpl(
            TxRunner tx,
            GameRepository games,
            MembershipRepository members,
            MoveRepository moves,
            LinksRepository links,
            VisitedArticlesRepository visited,
            ArticlesRepository articles,
            WikipediaService wikipedia
    ) {
        this.tx = Objects.requireNonNull(tx);
        this.games = Objects.requireNonNull(games);
        this.members = Objects.requireNonNull(members);
        this.moves = Objects.requireNonNull(moves);
        this.links = Objects.requireNonNull(links);
        this.visited = Objects.requireNonNull(visited);
        this.articles = Objects.requireNonNull(articles);
        this.wikipedia = Objects.requireNonNull(wikipedia);
    }

@Override
public CreateGameResult createGame(String playerIdString, String startTitle, String targetTitle) {
    requireNonBlank(playerIdString, "playerId");
    requireNonBlank(startTitle, "startTitle");
    requireNonBlank(targetTitle, "targetTitle");

    // Validate via wiki first (cheap, avoids creating dead games)
    if (!wikipedia.validateGamePages(startTitle, targetTitle)) {
        throw new IllegalArgumentException("Start or target page does not exist");
    }

    return tx.inTransaction(conn -> {
long pid = members.ensurePlayer(conn, playerIdString);
        // Resolve wiki page info (gets canonical title + page id)
    java.util.Map<String, Object> startInfo  = wikipedia.getPageInfo(startTitle);
    java.util.Map<String, Object> targetInfo = wikipedia.getPageInfo(targetTitle);

        if (Boolean.FALSE.equals(startInfo.get("exists")) || Boolean.FALSE.equals(targetInfo.get("exists"))) {
            throw new IllegalArgumentException("Start or target page does not exist");
        }

        long startWikiId  = ((Number) startInfo.get("id")).longValue();
        long targetWikiId = ((Number) targetInfo.get("id")).longValue();

        String canonicalStart  = (String) startInfo.get("title");
        String canonicalTarget = (String) targetInfo.get("title");

        // Store/resolve into your article ids (needs ArticlesRepository)
        long startArticleId  = articles.upsertArticle(conn, startWikiId, canonicalStart, canonicalStart);
        long targetArticleId = articles.upsertArticle(conn, targetWikiId, canonicalTarget, canonicalTarget);

        long gid = games.insertGame(conn, startArticleId, targetArticleId, pid, "PENDING");
        members.addPlayer(conn, gid, pid);

        // Optional warm cache
        wikipedia.getLinksFromPage(canonicalStart);

        GameState state = games.loadGameState(conn, gid);
        return new CreateGameResult(Long.toString(gid), state);
    });
}

public GameState joinGame(String playerIdString, String gameIdString) {
    requireNonBlank(playerIdString, "playerId");
    requireNonBlank(gameIdString, "gameId");

    return tx.inTransaction(conn -> {
        long gid = Long.parseLong(gameIdString);

        if (!games.gameExists(conn, gid)) {
            throw new IllegalArgumentException("Unknown gameId: " + gameIdString);
        }

        long pid = members.ensurePlayer(conn, playerIdString);
        members.addPlayer(conn, gid, pid);

        return games.loadGameState(conn, gid);
    });
}

public GameState startGame(String playerIdString, String gameIdString) {
    requireNonBlank(playerIdString, "playerId");
    requireNonBlank(gameIdString, "gameId");

    return tx.inTransaction(conn -> {
        long gid = Long.parseLong(gameIdString);

        if (!games.gameExists(conn, gid)) {
            throw new IllegalArgumentException("Unknown gameId: " + gameIdString);
        }

        long pid = members.ensurePlayer(conn, playerIdString);

        // Optional but recommended: only players in the game can start it
        if (!members.isInGame(conn, gid, pid)) {
            throw new IllegalArgumentException("Player is not in this game");
        }

        // Transition to ACTIVE (idempotent)
        games.startGame(conn, gid);

        return games.loadGameState(conn, gid);
    });
}

@Override
public MoveResult applyMove(String gameIdStr, String playerIdStr, long fromId, long toId) {
    requireNonBlank(gameIdStr, "gameId");
    requireNonBlank(playerIdStr, "playerId");

    return tx.inTransaction(conn -> {
        long gid = Ids.parseLongId(gameIdStr, "gameId");
        long pid = Ids.parseLongOrLookupPlayerId(conn, playerIdStr);

        // 1) started?
        String gameState = games.getGameState(conn, gid);
        if (!"ACTIVE".equalsIgnoreCase(gameState) && !"STARTED".equalsIgnoreCase(gameState)) {
            throw new IllegalArgumentException("Game not started (state=" + gameState + ")");
        }


        // 2) membership?
        if (!members.isMember(conn, gid, pid)) throw new IllegalArgumentException("Player not in game");

        // 3) server-derived current page
        Long last = moves.findLastToArticle(conn, gid, pid);
        long current = (last != null) ? last : games.getStartArticleId(conn, gid);

        if (current != fromId) {
            throw new IllegalArgumentException("Illegal move: fromArticleId=" + fromId + " but current page is " + current);
        }

        // 4) validate edge (DB first, then Wikipedia fallback)
        if (!links.linkExists(conn, fromId, toId)) {
            String fromTitle = articles.getTitleById(conn, fromId);
            String toTitle   = articles.getTitleById(conn, toId);

            if (!wikipedia.isValidMove(fromTitle, toTitle)) {
                throw new IllegalArgumentException("Illegal move: no link from " + fromId + " to " + toId);
            }

            // cache the discovered link
            links.insertLink(conn, fromId, toId);
        }

        // 5) insert move
        int moveSeq = moves.nextMoveSeq(conn, gid);
        long moveId = moves.insertMove(conn, gid, pid, moveSeq, fromId, toId, "OK");

        // 6) derived updates
        members.incrementStepsTaken(conn, gid, pid);
        visited.upsertVisited(conn, gid, toId, moveId);

        // 7) win check
        games.tryFinishIfTargetReached(conn, gid, pid, toId);

        // 8) return new state snapshot
        GameState newState = games.loadGameState(conn, gid);
        return new MoveResult(gameIdStr, playerIdStr, fromId, toId, newState);
    });
}

    private static void requireNonBlank(String s, String field) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing " + field);
        }
    }
}