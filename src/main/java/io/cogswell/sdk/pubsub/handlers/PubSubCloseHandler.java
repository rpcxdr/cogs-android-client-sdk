package io.cogswell.sdk.pubsub.handlers;

/**
 * Represents a handler function for Cogswell Pub/Sub close events.
 */
public interface PubSubCloseHandler {
    /**
     * Invoked as the initial call when shutting down an instance of {@link io.cogswell.sdk.pubsub.PubSubSocket}.
     *
     * @param error Error that caused the invocation of the handler.
     */
    void onClose(Throwable error);
}