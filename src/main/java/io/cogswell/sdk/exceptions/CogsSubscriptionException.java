package io.cogswell.sdk.exceptions;

/**
 * Created by jedwards on 5/4/16.
 */
public class CogsSubscriptionException extends RuntimeException {
    public CogsSubscriptionException(String message) {
        super(message);
    }

    public CogsSubscriptionException(String message, Throwable t) {
        super(message, t);
    }
}
