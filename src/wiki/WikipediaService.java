package wiki;

import wiki.exception.*;
import java.util.*;
import wiki.LinkProvider;
/**
 * Main service class that Game Service will use
 */
public class WikipediaService {
    private final WikiClient client;
    private final LinkProvider linkProvider;
    
    public WikipediaService() {
        this("https://en.wikipedia.org/w/api.php");
    }
    
    public WikipediaService(String apiUrl) {
        this.client = new WikiClient(apiUrl);
        this.linkProvider = new LinkProvider(client);
    }
    
    // ============= PUBLIC API FOR GAME SERVICE =============
    
    /**
     * Check if a move is valid (link exists)
     */
    public boolean isValidMove(String fromPage, String toPage) {
        return linkProvider.isValidMove(fromPage, toPage);
    }
    
    /**
     * Get all links from a page
     */
    public List<String> getLinksFromPage(String title) {
        try {
            return linkProvider.getLinksFromPage(title);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Check if a page exists
     */
    public boolean pageExists(String title) {
        return client.pageExists(title);
    }
    
    /**
     * Validate both start and target pages exist
     */
    public boolean validateGamePages(String startPage, String targetPage) {
        return pageExists(startPage) && pageExists(targetPage);
    }
    
    /**
     * Get page info
     */
    public Map<String, Object> getPageInfo(String title) {
        try {
            return client.getPageByTitle(title);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("exists", false);
            return error;
        }
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Integer> getCacheStats() {
        return client.getCacheStats();
    }
    
    // ============= END PUBLIC API =============
}