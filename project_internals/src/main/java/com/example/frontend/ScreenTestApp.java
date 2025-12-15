package ui;

import com.example.persistence.DatabaseInitializer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.frontend.util.WsClientService;

import com.example.persistence.DuckDbGameRepository;
import com.example.persistence.DuckDbMembershipRepository;
import com.example.persistence.DuckDbMoveRepository;
import com.example.persistence.GameRepository;
import com.example.persistence.LinksRepository;
import com.example.persistence.LinksRepositoryImpl;
import com.example.persistence.MembershipRepository;
import com.example.persistence.MoveRepository;
import com.example.persistence.TxRunner;
import com.example.persistence.VisitedArticlesRepository;
import com.example.persistence.VisitedArticlesRepositoryImpl;

import com.example.frontend.AppContext;
import com.example.core.domain.ArticlesRepository;
import com.example.core.domain.ArticlesRepositoryImpl;
import com.example.core.domain.GameCommandServiceImpl;

import com.example.persistence.*;

import wiki.WikipediaService;

public class ScreenTestApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("[GUI] Starting ScreenTestApp");

            // 1) DB/schema boot (same as server)
            DatabaseInitializer.initialize();

            // 2) Build domain service (same as server)
            AppContext.game = buildGameService();
            AppContext.ws = new WsClientService("ws://localhost:8080"); // use your actual WS port
            AppContext.ws.connect();
            // 3) Load UI
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/LandingView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    ScreenTestApp.class.getResource("/styles/app.css").toExternalForm()
            );

            stage.setTitle("Wikipedia Race");
            stage.setScene(scene);
            stage.show();

            System.out.println("[GUI] LandingView loaded successfully");

        } catch (Exception e) {
            System.err.println("[GUI] FAILED TO LOAD GUI");
            e.printStackTrace();
        }
    }
     
    private GameCommandServiceImpl buildGameService() {
        try {
            // Repos
            MembershipRepository playerRepo = new DuckDbMembershipRepository();
            GameRepository gameRepo = new DuckDbGameRepository(playerRepo);
            MoveRepository movesRepo = new DuckDbMoveRepository();
            LinksRepository linksRepo = new LinksRepositoryImpl();
            VisitedArticlesRepository visitedRepo = new VisitedArticlesRepositoryImpl();
            ArticlesRepository articleRepo = new ArticlesRepositoryImpl();

            // TxRunner needs a ConnectionProvider
            TxRunner txRunner = new TxRunner(DatabaseInitializer::getConnection);

            // Minimal WikipediaService stub (implement methods your interface requires)
            WikipediaService wikiService = new WikipediaService() {
                // TODO: implement required methods; for now:
                // @Override public X method(...) { throw new UnsupportedOperationException("TODO"); }
            };

            return new GameCommandServiceImpl(
                    txRunner,
                    gameRepo,
                    playerRepo,
                    movesRepo,
                    linksRepo,
                    visitedRepo,
                    articleRepo,
                    wikiService
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to build GameCommandService for GUI", e);
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}