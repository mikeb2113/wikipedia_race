package com.example.websocket;

/*import com.example.core.GameService;
import com.example.core.InMemoryGameService;*/
import com.example.websocket.WebSocketMessageHandler;

import main.java.com.example.core.domain.GameCommandService;
import main.java.com.example.core.domain.GameCommandServiceImpl;
import com.example.persistence.LinksRepository;
import com.example.persistence.VisitedArticlesRepository;

import com.example.persistence.LinksRepository;
import com.example.persistence.VisitedArticlesRepository;

import com.example.persistence.LinksRepositoryImpl;
import com.example.persistence.VisitedArticlesRepositoryImpl;

import com.example.persistence.ConnectionProvider;
import com.example.persistence.GameRepository;
import com.example.persistence.MembershipRepository;
import com.example.persistence.MoveRepository;
import com.example.persistence.TxRunner;
import com.example.persistence.DatabaseInitializer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import com.example.persistence.DuckDbGameRepository;
import com.example.persistence.DuckDbMembershipRepository;
import com.example.persistence.DuckDbMoveRepository;
import java.net.InetSocketAddress;

public class GameWebSocketServer extends WebSocketServer {

    private final WebSocketMessageHandler handler;
    private final ConnectionRegistry registry = new ConnectionRegistry();

public GameWebSocketServer(int port) {
    super(new InetSocketAddress(port));

    MembershipRepository memberRepo = new DuckDbMembershipRepository();
    GameRepository gameRepo = new DuckDbGameRepository(memberRepo);
    MoveRepository moveRepo = new DuckDbMoveRepository();

    // ✅ add these two
    LinksRepositoryImpl linksRepo = new LinksRepositoryImpl();
    VisitedArticlesRepositoryImpl visitedRepo = new VisitedArticlesRepositoryImpl();

    // ---- Persistence wiring ----
    ConnectionProvider cp = () -> DatabaseInitializer.getConnection();
    TxRunner tx = new TxRunner(cp);

    GameCommandService gameService =
        new GameCommandServiceImpl(tx, gameRepo, memberRepo, moveRepo, linksRepo, visitedRepo);

    // ---- WebSocket handler ----
    this.handler = new WebSocketMessageHandler(gameService);
}


    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("WebSocket opened: " + conn.getRemoteSocketAddress());
        // nothing to bind yet; we learn player/game from messages
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            // Let handler process, but ALSO allow it to broadcast via registry
            String responseJson = handler.handleIncoming(message, conn, registry);

            if (responseJson != null) {
                conn.send(responseJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
            conn.send("{\"type\":\"ERROR\",\"code\":\"INTERNAL_ERROR\",\"message\":\"Unexpected server error.\"}");
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + reason + " from " + conn.getRemoteSocketAddress());
        registry.removeConn(conn); // ✅ cleanup
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on " + getAddress());
    }
}