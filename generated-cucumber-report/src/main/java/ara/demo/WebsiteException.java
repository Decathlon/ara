package ara.demo;

public class WebsiteException extends Exception {

    public WebsiteException(String message) {
        super("Website displayed an error message: " + message);
    }

}
