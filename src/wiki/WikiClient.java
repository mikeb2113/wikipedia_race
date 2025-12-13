package wiki;

import wiki.exception.*;
import com.google.gson.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WikiClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final CacheRepo cache; 
    
    public WikiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.cache = new CacheRepo();  
    }
    
    //get page by  title
    public Map<String, Object> getPageByTitle(String title) throws WikipediaException {
        String normalized = normalize(title);
        
        // Check cache
        Object cached = cache.getCachedPage(normalized);
        if (cached != null) {
            System.out.println("Cache hit: " + normalized);
            return (Map<String, Object>) cached;
        }
        
        // Build URL
        String url = baseUrl + "?action=query&titles=" + 
            URLEncoder.encode(normalized, java.nio.charset.StandardCharsets.UTF_8) +
            "&format=json&redirects=true";
        
        System.out.println("Fetching: " + normalized);
        
        // Make request
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "WikiRaceGame/1.0")
                .timeout(Duration.ofSeconds(10))
                .build();
            
            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
            
            // Parse JSON
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject query = json.getAsJsonObject("query");
            
            if (query == null) {
                throw new PageNotFoundException(title);
            }
            
            JsonObject pages = query.getAsJsonObject("pages");
            String pageId = pages.keySet().iterator().next();
            JsonObject pageObj = pages.getAsJsonObject(pageId);
            
            // Check if missing
            if (pageObj.has("missing")) {
                throw new PageNotFoundException(title);
            }
            
            // Extract data
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("id", Long.parseLong(pageId));
            pageData.put("title", pageObj.get("title").getAsString());
            pageData.put("exists", true);
            
            // Cache it
            cache.cachePage(normalized, pageData);
            return pageData;
  
            
        } catch (PageNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new WikipediaException("API error: " + e.getMessage());
        }
    }
    
    /**
     * Check if page exists
     */
    public boolean pageExists(String title) {
        try {
            getPageByTitle(title);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get links from a page
     */
    public List<String> getLinksFromPage(String title) throws WikipediaException {
        String normalized = normalize(title);
        
        // Check cache
        Object cached = cache.getCachedLinks(normalized);
        if (cached != null) {
            System.out.println("Cache hit (links): " + normalized);
            return (List<String>) cached;
        }
        
        // Build URL
        String url = baseUrl + "?action=query&titles=" + 
            URLEncoder.encode(normalized, java.nio.charset.StandardCharsets.UTF_8) +
            "&prop=links&pllimit=500&format=json";
        
        System.out.println("Fetching links: " + normalized);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "WikiRaceGame/1.0")
                .timeout(Duration.ofSeconds(10))
                .build();
            
            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
            
            // Parse JSON
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject query = json.getAsJsonObject("query");
            
            if (query == null) {
                throw new PageNotFoundException(title);
            }
            
            JsonObject pages = query.getAsJsonObject("pages");
            String pageId = pages.keySet().iterator().next();
            JsonObject pageObj = pages.getAsJsonObject(pageId);
            
            if (pageObj.has("missing")) {
                throw new PageNotFoundException(title);
            }
            
            // Extract links
            List<String> links = new ArrayList<>();
            
            if (pageObj.has("links")) {
                JsonArray linksArray = pageObj.getAsJsonArray("links");
                for (JsonElement linkElement : linksArray) {
                    JsonObject linkObj = linkElement.getAsJsonObject();
                    String linkTitle = linkObj.get("title").getAsString();
                    
                    // Filter out invalid links
                    if (!linkTitle.startsWith("Category:") &&
                        !linkTitle.startsWith("File:") &&
                        !linkTitle.startsWith("Template:") &&
                        !linkTitle.startsWith("Wikipedia:")) {
                        links.add(normalize(linkTitle));
                    }
                }
            }
            
            // Sort and cache
            cache.cacheLinks(normalized, links);
        
            return links;
    
            
        } catch (PageNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new WikipediaException("API error: " + e.getMessage());
        }
    }
    
    /**
     * Normalize title (spaces to underscores, trim)
     */
    private String normalize(String title) {
        if (title == null) return "";
        return title.trim().replace(' ', '_');
    }
    /**
     * Get cache stats
     */
    public Map<String, Integer> getCacheStats() {
        return cache.getStats();  // ← Now returns detailed stats
    }
}
