package com.decathlon.ara.ci.util;

public class FetchException extends Exception {

    private static final long serialVersionUID = 2807142911588081627L;

    public FetchException(Exception e, String url) {
        this(e, e.getMessage(), url);
    }

    public FetchException(Exception e, String message, String url) {
        super("Cannot fetch \"" + url + "\": " + message, e);
    }

    public FetchException(String message, String url) {
        super("Cannot fetch \"" + url + "\": " + message);
    }

    public FetchException(String message) {
        super(message);
    }

    public FetchException(String message, Throwable t) {
        super(message, t);
    }

}
