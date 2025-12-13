package main.java.com.example.core.domain;
import main.java.com.example.core.domain.GameState;
import com.example.persistence.LinksRepository;
import com.example.persistence.VisitedArticlesRepository;

import com.example.persistence.Ids;

import com.example.persistence.GameRepository;
import com.example.persistence.MembershipRepository;
import com.example.persistence.MoveRepository;
import com.example.persistence.TxRunner;

import java.util.Objects;

public class GameCommandServiceImpl implements GameCommandService {

    private final TxRunner tx;
    private final GameRepository games;
    private final MembershipRepository members;
    private final MoveRepository moves;
    private final LinksRepository links;
    private final VisitedArticlesRepository visited;
    public GameCommandServiceImpl(TxRunner tx,
                              GameRepository games,
                              MembershipRepository members,
                              MoveRepository moves,
                              LinksRepository links,
                              VisitedArticlesRepository visited) {
    this.tx = Objects.requireNonNull(tx);
    this.games = Objects.requireNonNull(games);
    this.members = Objects.requireNonNull(members);
    this.moves = Objects.requireNonNull(moves);
    this.links = Objects.requireNonNull(links);
    this.visited = Objects.requireNonNull(visited);
}

    @Override
    public CreateGameResult createGame(String playerIdString) {
        requireNonBlank(playerIdString, "playerId");

        return tx.inTransaction(conn -> {
            long pid = Ids.parseLongOrLookupPlayerId(conn, playerIdString);

            // TODO: choose these properly (maybe from msg.payload later)
            long startArticleId = 5;
            Long targetArticleId = 7L;

            long gid = games.insertGame(conn, startArticleId, targetArticleId, pid, "PENDING");
            members.addPlayer(conn, gid, pid);

            GameState state = games.loadGameState(conn, gid);
            return new CreateGameResult(Long.toString(gid), state);
        });
    }

    @Override
    public GameState joinGame(String gameIdString, String playerIdString) {
        requireNonBlank(gameIdString, "gameId");
        requireNonBlank(playerIdString, "playerId");

        return tx.inTransaction(conn -> {
            long gid = Ids.parseLongId(gameIdString, "gameId");
            long pid = Ids.parseLongOrLookupPlayerId(conn, playerIdString);

            if (!games.gameExists(conn, gid)) {
                throw new IllegalArgumentException("Game not found: " + gid);
            }

            String status = games.getGameState(conn, gid);
            if ("FINISHED".equals(status)) {
                throw new IllegalArgumentException("Game already finished");
            }

            members.addPlayer(conn, gid, pid);
            return games.loadGameState(conn, gid);
        });
    }

    @Override
    public GameState startGame(String gameIdString, String playerIdString) {
        requireNonBlank(gameIdString, "gameId");
        requireNonBlank(playerIdString, "playerId");

        return tx.inTransaction(conn -> {
            long gid = Ids.parseLongId(gameIdString, "gameId");
            long pid = Ids.parseLongOrLookupPlayerId(conn, playerIdString);

            if (!games.gameExists(conn, gid)) {
                throw new IllegalArgumentException("Game not found: " + gid);
            }

            if (!members.isMember(conn, gid, pid)) {
                throw new IllegalArgumentException("Player not in game");
            }

            String status = games.getGameState(conn, gid);
            if ("STARTED".equals(status)) return games.loadGameState(conn, gid);
            if ("FINISHED".equals(status)) throw new IllegalArgumentException("Game already finished");

            games.setGameStarted(conn, gid);
            return games.loadGameState(conn, gid);
        });
    }

@Override
public MoveResult applyMove(String gameIdStr, String playerIdStr,
                            long fromId, long toId) {

    requireNonBlank(gameIdStr, "gameId");
    requireNonBlank(playerIdStr, "playerId");

    return tx.inTransaction(conn -> {
        long gid = Ids.parseLongId(gameIdStr, "gameId");
        long pid = Ids.parseLongOrLookupPlayerId(conn, playerIdStr);

        // 1) Game must be started
        String gameState = games.getGameState(conn, gid);
        if (!"STARTED".equals(gameState)) {
            throw new IllegalArgumentException("Game not started");
        }

        // 2) Player must be in game
        if (!members.isMember(conn, gid, pid)) {
            throw new IllegalArgumentException("Player not in game");
        }

        // 3) Derive current page
        Long last = moves.findLastToArticle(conn, gid, pid);
        long current = (last != null)
                ? last
                : games.getStartArticleId(conn, gid);

        if (current != fromId) {
            throw new IllegalArgumentException(
                "Illegal move: fromArticleId=" + fromId +
                " but current page is " + current
            );
        }

        // 4) Link must exist
        if (!links.linkExists(conn, fromId, toId)) {
            throw new IllegalArgumentException(
                "Illegal move: no link from " + fromId + " to " + toId
            );
        }

        // 5) Insert move
        int moveSeq = moves.nextMoveSeq(conn, gid);
        long moveId = moves.insertMove(conn, gid, pid,
                                       moveSeq, fromId, toId, "OK");

        // 6) Derived updates
        members.incrementStepsTaken(conn, gid, pid);
        visited.upsertVisited(conn, gid, toId, moveId);

        // 7) Win condition
        games.tryFinishIfTargetReached(conn, gid, pid, toId);

        // 8) Return new state
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