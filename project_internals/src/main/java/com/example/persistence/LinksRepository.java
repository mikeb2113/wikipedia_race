package com.example.persistence;

import java.sql.Connection;
import java.util.List;

public interface LinksRepository {
    List<Long> getNeighbors(Connection conn, long fromArticleId) throws Exception;

    // for applyMove legality check:
    boolean linkExists(Connection conn, long fromId, long toId) throws Exception;
}