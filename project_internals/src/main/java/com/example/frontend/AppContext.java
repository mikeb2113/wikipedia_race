package com.example.frontend;

import com.example.frontend.util.WsClientService;
import com.example.core.domain.GameCommandService;

public class AppContext {
public static String pendingCreateReqId;
public static String lastCreatedGameId;
    public static WsClientService ws;            // keep for later
    public static GameCommandService game;       // NEW: direct/local mode
        public static String playerId;   // <-- add this
}