package io.cogswell.sdk.exceptions;

import io.cogswell.sdk.subscription.CogsSubscriptionRequest;

/**
 * Created by jedwards on 5/3/16.
 *
 * This is a runtime exception used to signal a failure when a builder attempts to
 * assemble a resource.
 */
public class CogsBuilderException extends RuntimeException {
    public CogsBuilderException(String message) {
        super(message);
    }

    public CogsBuilderException(String message, Throwable t) {
        super(message, t);
    }
}
