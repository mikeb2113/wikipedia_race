package com.example.websocket;

import java.util.Map;

public class WsEnvelope {
    public MessageType type;
    public String requestId;
    public String playerId;
    public String gameId;

    // generic payload for now (you can make specific classes later)
    public Map<String, Object> payload;
}