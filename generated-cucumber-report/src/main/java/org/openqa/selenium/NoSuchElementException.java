package org.openqa.selenium;

/**
 * Do not import the whole Selenium library, but show the real exception class in ARA reports.
 */
public class NoSuchElementException extends RuntimeException {

    public NoSuchElementException(String s) {
        super(s);
    }

}
