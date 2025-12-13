package wiki.exception;

public class PageNotFoundException extends WikipediaException {
    public PageNotFoundException(String title) {
        super("Page not found: " + title);
    }
}
