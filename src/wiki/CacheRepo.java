
package wiki;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache repository for Wikipedia data with TTL (Time To Live) support
 * Thread-safe implementation using ConcurrentHashMap
 */
public class CacheRepo {
    private final Map<String, CacheEntry> cache;
    
    // TTL constants (in milliseconds)
    private static final long PAGE_TTL = 24 * 60 * 60 * 1000;         // 24 hours
    private static final long LINK_TTL = 12 * 60 * 60 * 1000;         // 12 hours
    private static final long REDIRECT_TTL = 7 * 24 * 60 * 60 * 1000; // 7 days
    
    /**
     * Constructor - initializes empty cache
     */
    public CacheRepo() {
        this.cache = new ConcurrentHashMap<>();
    }
    
    // ==================== PAGE CACHE ====================
    
    /**
     * Cache a page with 24-hour TTL
     * @param title Page title (normalized)
     * @param pageData Page data object
     */
    public void cachePage(String title, Object pageData) {
        if (title == null || pageData == null) {
            throw new IllegalArgumentException("Title and pageData cannot be null");
        }
        String key = buildKey("page", title);
        cache.put(key, new CacheEntry(pageData, PAGE_TTL));
    }
    
    /**
     * Get cached page if not expired
     * @param title Page title
     * @return Page data or null if not cached/expired
     */
    public Object getCachedPage(String title) {
        if (title == null) return null;
        String key = buildKey("page", title);
        return getIfValid(key);
    }
    
    // ==================== LINK CACHE ====================
    
    /**
     * Cache links with 12-hour TTL
     * @param title Page title
     * @param linksData Links data (List<String>)
     */
    public void cacheLinks(String title, Object linksData) {
        if (title == null || linksData == null) {
            throw new IllegalArgumentException("Title and linksData cannot be null");
        }
        String key = buildKey("links", title);
        cache.put(key, new CacheEntry(linksData, LINK_TTL));
    }
    
    /**
     * Get cached links if not expired
     * @param title Page title
     * @return Links data or null if not cached/expired
     */
    public Object getCachedLinks(String title) {
        if (title == null) return null;
        String key = buildKey("links", title);
        return getIfValid(key);
    }
    
    // ==================== REDIRECT CACHE ====================
    
    /**
     * Cache redirect mapping with 7-day TTL
     * @param from Source page title
     * @param to Destination page title
     */
    public void cacheRedirect(String from, String to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From and to cannot be null");
        }
        if (from.equals(to)) {
            throw new IllegalArgumentException("Cannot cache self-redirect");
        }
        String key = buildKey("redirect", from);
        cache.put(key, new CacheEntry(to, REDIRECT_TTL));
    }
    
    /**
     * Get cached redirect mapping if not expired
     * @param from Source page title
     * @return Destination title or null if not cached/expired
     */
    public Object getCachedRedirect(String from) {
        if (from == null) return null;
        String key = buildKey("redirect", from);
        return getIfValid(key);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get value if not expired
     * @param key Cache key
     * @return Cached value or null
     */
    private Object getIfValid(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        
        return entry.data;
    }
    
    /**
     * Build cache key in format: wiki:type:identifier
     * @param type Cache type (page/links/redirect)
     * @param identifier Page title or key
     * @return Formatted cache key
     */
    private String buildKey(String type, String identifier) {
        return "wiki:" + type + ":" + identifier;
    }
    
    /**
     * Invalidate/remove a specific key
     * @param key Full cache key
     */
    public void invalidate(String key) {
        cache.remove(key);
    }
    
    /**
     * Clear entire cache
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Get cache statistics
     * @return Map with cache stats
     */
    public Map<String, Integer> getStats() {
        // Clean expired entries first
        cleanExpired();
        
        Map<String, Integer> stats = new HashMap<>();
        
        // Count by type
        int pageCount = 0;
        int linkCount = 0;
        int redirectCount = 0;
        
        for (String key : cache.keySet()) {
            if (key.startsWith("wiki:page:")) pageCount++;
            else if (key.startsWith("wiki:links:")) linkCount++;
            else if (key.startsWith("wiki:redirect:")) redirectCount++;
        }
        
        stats.put("totalEntries", cache.size());
        stats.put("pageCount", pageCount);
        stats.put("linkCount", linkCount);
        stats.put("redirectCount", redirectCount);
        
        return stats;
    }
    
    /**
     * Remove all expired entries
     */
    private void cleanExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Check if a key exists and is not expired
     * @param type Cache type
     * @param identifier Key identifier
     * @return true if exists and valid
     */
    public boolean contains(String type, String identifier) {
        String key = buildKey(type, identifier);
        CacheEntry entry = cache.get(key);
        return entry != null && !entry.isExpired();
    }
    
    // ==================== INNER CLASS ====================
    
    /**
     * Cache entry with data and expiration time
     */
    private static class CacheEntry {
        final Object data;
        final long timestamp;
        final long ttl;
        
        CacheEntry(Object data, long ttl) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > (timestamp + ttl);
        }
        
        long getAge() {
            return System.currentTimeMillis() - timestamp;
        }
    }
}