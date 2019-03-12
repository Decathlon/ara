package com.decathlon.ara.common;

/**
 * Used in places, where a checked java exception is reported by the compiler but the programmer knows, that such an Exception will never be
 * thrown there.
 * <p>
 * This exception is only used to re-throw an exception and is an unchecked exception.
 *
 * @see <a href= "https://github.com/scravy/Obsolete/blob/master/AbusingJava/src/main/java/net/abusingjava/NotGonnaHappenException.java">GitHub
 * source</a>
 */
public class NotGonnaHappenException extends RuntimeException {

    private static final long serialVersionUID = 2807142911588081627L;

    /**
     * Constructor to throw an Exception in a branch of code that is not supposed to run.
     *
     * @param message The message/hint
     */
    public NotGonnaHappenException(final String message) {
        super(message);
    }

    /**
     * Constructor to re-throw an Exception with a more detailed message (e.g. a hint of what could have happened here).
     *
     * @param message The message/hint
     * @param cause   The exception
     */
    public NotGonnaHappenException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
