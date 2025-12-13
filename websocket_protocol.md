# WebSocket Protocol – Wikipedia Race

All WebSocket messages are **JSON objects** with at least:

- `"type"` – the message kind (string)
- `"gameId"` – which game/race it refers to (when applicable)
- `"playerId"` – the server-assigned player ID (when applicable)

Clients **only** talk to the server via WebSocket.  
Server broadcasts updates back to all relevant clients.

---

## 1. Connection & Identity

### 1.1 Client → Server: `HELLO`

Sent immediately after opening the WebSocket, so the server can register the player.

```json
{
  "type": "HELLO",
  "playerName": "Michael"
}
```
### 1.2 Server → Client: `WELCOME`

```json
{
  "type": "WELCOME",
  "playerId": "p-1283",
  "playerName": "Michael"
}
```
==========================================================================================

## 2. Lobby & Game Management

### 2.1 Client → Server: `CREATE_GAME`
```json

{
  "type": "CREATE_GAME",
  "playerId": "p-1283",
  "startArticle": "Alan Turing",
  "targetArticle": "World War II",
  "maxPlayers": 4
}
```
### 2.2 Server → Client: `GAME_CREATED`
```json

{
  "type": "GAME_CREATED",
  "gameId": "g-42",
  "hostId": "p-1283",
  "startArticle": "Alan Turing",
  "targetArticle": "World War II",
  "maxPlayers": 4,
  "status": "WAITING_FOR_PLAYERS"
}
```
### 2.3 Client → Server: `JOIN_GAME`
```json

{
  "type": "JOIN_GAME",
  "playerId": "p-0099",
  "gameId": "g-42"
}
```
### 2.4 Server → Client: `GAME_JOINED`
```json

{
  "type": "GAME_JOINED",
  "gameId": "g-42",
  "playerId": "p-0099",
  "players": [
    { "id": "p-1283", "name": "Michael" },
    { "id": "p-0099", "name": "Teammate" }
  ],
  "startArticle": "Alan Turing",
  "targetArticle": "World War II",
  "status": "WAITING_FOR_PLAYERS"
}
```
### 2.5 Server → Client: `LOBBY_UPDATE`
```json

{
  "type": "LOBBY_UPDATE",
  "gameId": "g-42",
  "players": [
    { "id": "p-1283", "name": "Michael" },
    { "id": "p-0099", "name": "Teammate" }
  ],
  "status": "WAITING_FOR_PLAYERS"
}
```
### 2.6 Client → Server: `START_GAME`
```json

{
  "type": "START_GAME",
  "playerId": "p-1283",
  "gameId": "g-42"
}
```
### 2.7 Server → Client: `GAME_STARTED`
```json

{
  "type": "GAME_STARTED",
  "gameId": "g-42",
  "startArticle": "Alan Turing",
  "targetArticle": "World War II",
  "startTime": 1732812000000
}
```

### 2.8 Client → Server: `LEAVE_GAME`
```json
{
  "type": "LEAVE_GAME",
  "playerId": "p-0099",
  "gameId": "g-42"
}
```

### 2.9 Server → Client: `PLAYER_LEFT`
```json
{
  "type": "PLAYER_LEFT",
  "gameId": "g-42",
  "playerId": "p-0099"
}
```
==========================================================================================

## 3. Player Actions & Game Progress

### 3.1 Client → Server: `PLAYER_MOVE`
```json

{
  "type": "PLAYER_MOVE",
  "playerId": "p-0099",
  "gameId": "g-42",
  "fromArticle": "Alan Turing",
  "toArticle": "Enigma machine"
}
```
### 3.2 Server → Client: `PLAYER_MOVED`
```json

{
  "type": "PLAYER_MOVED",
  "gameId": "g-42",
  "playerId": "p-0099",
  "article": "Enigma machine",
  "pathLength": 2
}
```
### 3.3 Server → Client: `GAME_START`
```json

{
  "type": "GAME_STATE",
  "gameId": "g-42",
  "players": [
    { "id": "p-1283", "name": "Michael", "article": "Alan Turing", "pathLength": 1 },
    { "id": "p-0099", "name": "Teammate", "article": "Enigma machine", "pathLength": 2 }
  ],
  "targetArticle": "World War II",
  "status": "IN_PROGRESS"
}
```
### 3.4 Server → Client: `GAME_OVER`
```json

{
  "type": "GAME_OVER",
  "gameId": "g-42",
  "winnerId": "p-0099",
  "winnerName": "Teammate",
  "winnerPath": [
    "Alan Turing",
    "Enigma machine",
    "World War II"
  ],
  "finalState": {
    "players": [
      { "id": "p-1283", "name": "Michael", "article": "Alan Turing", "pathLength": 1 },
      { "id": "p-0099", "name": "Teammate", "article": "World War II", "pathLength": 3 }
    ]
  }
}
```
==========================================================================================

## 4. Utility

### 4.1 Client → Server: `PING`
```json

{
  "type": "PING"
}
```
### 4.2 Server → Client: `PONG`
```json

{
  "type": "PONG"
}
```

### 4.2 Client → Server: `REQUEST_STATE`
```json

{ 
  "type": "REQUEST_STATE", 
  "gameId": "g-42" 
}
```

==========================================================================================

## 5. Error Handling

### 5.1 Server → Client: `ERROR`
```json

{
  "type": "ERROR",
  "code": "GAME_NOT_FOUND",
  "message": "Game g-999 does not exist.",
  "requestType": "JOIN_GAME"
}
```
==========================================================================================

## 6. Server Responsibilities

  Parse incoming messages by "type".

  Track which sessions belong to which game.

  Handle:

      HELLO

      CREATE_GAME

      JOIN_GAME

      START_GAME

      PLAYER_MOVE

  Broadcast:

      LOBBY_UPDATE

      GAME_STARTED

      PLAYER_MOVED

      GAME_STATE

      GAME_OVER

  Send ERROR when requests are invalid.

==========================================================================================

## 7. Protocol Rules

    After connecting, client must send HELLO first.

    Server responds with WELCOME and playerId.

    Client may send CREATE_GAME or JOIN_GAME only after WELCOME.

    Only the game host (hostId) may send START_GAME.

    Client may send PLAYER_MOVE only when:

    it has successfully joined the game (GAME_JOINED), and

    the game status is "IN_PROGRESS".

    When the WebSocket closes, the server treats the player as if they sent LEAVE_GAME.

==========================================================================================

## 8. Message Routing

    WELCOME, GAME_CREATED, GAME_JOINED, ERROR → only to the requesting client.

    LOBBY_UPDATE → to all clients in that game’s lobby.

    GAME_STARTED, PLAYER_MOVED, GAME_STATE, GAME_OVER → to all clients in that game.

    PONG → only to the client that sent PING.

    If a websocket closes unexpectedly, treat it as if LEAVE_GAME was sent.