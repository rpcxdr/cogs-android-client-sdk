package io.cogswell.sdk.pubsub.handlers;

/**
 * Represents a handler function for whenever {@link io.cogswell.sdk.pubsub.PubSubSocket} re-established underlying connection.
 */
public interface PubSubReconnectHandler {
    /**
     * Invoked when the underlying socket of an instance of {@link io.cogswell.sdk.pubsub.PubSubSocket} is re-established.
     */
    void onReconnect();
}