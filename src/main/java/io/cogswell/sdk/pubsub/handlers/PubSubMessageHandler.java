package io.cogswell.sdk.pubsub.handlers;

import io.cogswell.sdk.pubsub.PubSubMessageRecord;

/**
 * Represents a handler function for dealing with message records from Cogswell Pub/Sub.
 */
public interface PubSubMessageHandler {
    /**
     * Invoked when a content message is received from Cogswell Pub/Sub.
     *
     * @param record PubSubMessageRecord filled with information about the message received.
     */
    void onMessage(PubSubMessageRecord record);
}