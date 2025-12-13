package com.example.persistence;

import java.util.ArrayList;
import java.util.List;
public interface LinksRepository {
    List<Long> getNeighbors(long fromArticleId);
    // later: ensureLinksPopulated(articleId) to call Wikipedia and fill `links`
}