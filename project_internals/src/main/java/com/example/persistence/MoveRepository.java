package com.example.persistence;

import java.sql.Connection;


public interface MoveRepository {

    int nextMoveSeq(Connection conn, long gameId) throws Exception;

    long insertMove(Connection conn,
                    long gameId,
                    long playerId,
                    int moveSeq,
                    long fromId,
                    long toId,
                    String status) throws Exception;

    // ✅ THIS must exist, exactly like this
    Long findLastToArticle(Connection conn, long gameId, long playerId) throws Exception;
}