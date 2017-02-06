package io.cogswell.sdk.pubsub;

import com.google.common.util.concurrent.ListenableFuture;

import io.cogswell.sdk.pubsub2.PubSubHandle;

public class CogsPubSub {
    /**
     * Open a connection to the Cogswell Pub/Sub system.
     *
     * @param keys the auth keys for the connection
     * @param options the PubSubOptions for customization
     *
     * @return a Future which, if successful, will contain a new PubSubHandle
     * for interacting with the server
     */
    public static ListenableFuture<PubSubHandle> connect(String[] keys, PubSubOptions options) {
        return null;//PubSubHandle.connect(keys, options);
    }
}
