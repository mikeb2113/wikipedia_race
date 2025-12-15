package com.example.frontend;

import com.example.frontend.util.WsClientService;
import com.example.core.domain.GameCommandService;

public class AppContext {
public static String pendingCreateReqId;
public static String lastCreatedGameId;
public static boolean gameStarted;
public static String currentGameId;
public static String pendingJoinReqId;
public static String lastJoinedGameId;
public static String lastGameStateJson;

public static String lastErrorJson; // optional

    public static WsClientService ws;            // keep for later
    public static GameCommandService game;       // NEW: direct/local mode
        public static String playerId;   // <-- add this
}