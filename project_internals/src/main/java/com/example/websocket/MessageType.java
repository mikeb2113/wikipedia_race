package com.example.websocket;

public enum MessageType {
    // Client -> Server
    HELLO,
    CREATE_GAME,
    JOIN_GAME,
    START_GAME,
    PLAYER_MOVE,
    LEAVE_GAME,
    PING,

    // Server -> Client
    ERROR,
    GAME_CREATED,
    GAME_STATE,
    PLAYER_JOINED,
    GAME_STARTED,
    MOVE_RESULT,
    GAME_OVER,
    PONG
}