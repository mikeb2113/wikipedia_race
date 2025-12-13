package wiki;

import wiki.exception.*;
import com.google.gson.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class LinkProvider {
    private final WikiClient client;
    private final String baseUrl;
    private final java.net.http.HttpClient httpClient;
    
    public LinkProvider(WikiClient client) {
        this.client = client;
        this.baseUrl = "https://en.wikipedia.org/w/api.php";
        this.httpClient = java.net.http.HttpClient.newHttpClient();
    }
    
    /**
     * Get all links from a page (first 500)
     */
    public List<String> getLinksFromPage(String title) throws WikipediaException {
        return client.getLinksFromPage(title);
    }
    
    /**
     * Check if there's a link from -> to
     * First checks cached 500 links, then makes specific API call if needed
     */
    public boolean hasLinkTo(String fromPage, String toPage) {
        try {
            // Step 1: Check cached links first (fast - in first 500)
            List<String> links = getLinksFromPage(fromPage);
            String normalized = normalize(toPage);
            
            if (links.contains(normalized)) {
                System.out.println("✓ Link found in cache");
                return true;
            }
            
            // Step 2: Not in first 500, check specifically with API
            System.out.println("Link not in first 500, checking directly with API...");
            return checkSpecificLink(fromPage, toPage);
            
        } catch (Exception e) {
            System.err.println("Error checking link: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a specific link exists between two pages
     * Makes a targeted API call asking "does fromPage link to toPage?"
     */
    private boolean checkSpecificLink(String fromPage, String toPage) {
        try {
            String normalizedFrom = normalize(fromPage);
            String normalizedTo = normalize(toPage);
            
            // Build API URL to check specific link
            String url = baseUrl + "?action=query&titles=" + 
                URLEncoder.encode(normalizedFrom, StandardCharsets.UTF_8) +
                "&prop=links&pltitles=" + 
                URLEncoder.encode(normalizedTo, StandardCharsets.UTF_8) +
                "&format=json";
            
            // Make request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "WikiRaceGame/1.0")
                .timeout(Duration.ofSeconds(10))
                .build();
            
            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
            
            // Parse JSON
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            
            // Check if query succeeded
            if (!json.has("query")) {
                return false;
            }
            
            JsonObject query = json.getAsJsonObject("query");
            
            // Get pages object
            if (!query.has("pages")) {
                return false;
            }
            
            JsonObject pages = query.getAsJsonObject("pages");
            
            // Get first page (there should only be one)
            String pageId = pages.keySet().iterator().next();
            JsonObject pageObj = pages.getAsJsonObject(pageId);
            
            // Check if page was found and has links
            if (pageObj.has("missing")) {
                return false; // Page doesn't exist
            }
            
            // If "links" array exists and has items, the link exists!
            if (pageObj.has("links")) {
                JsonArray linksArray = pageObj.getAsJsonArray("links");
                System.out.println("✓ Link confirmed via API (link #" + (500 + 1) + "+)");
                return linksArray.size() > 0;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Error in specific link check: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate a move in the game
     */
    public boolean isValidMove(String currentPage, String targetPage) {
        return hasLinkTo(currentPage, targetPage);
    }
    
    /**
     * Normalize page title (trim and replace spaces with underscores)
     */
    private String normalize(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }
        return title.trim().replace(' ', '_');
    }
}