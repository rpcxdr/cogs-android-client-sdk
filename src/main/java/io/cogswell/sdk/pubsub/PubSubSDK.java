package io.cogswell.sdk.pubsub;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;


import java.util.List;
import java.util.concurrent.Executor;

/**
 * All initial connections made to Cogswell Pub/Sub are preformed through this class.
 * Thereafter, all operations are preformed through an instance of {@link PubSubHandle}
 */
public class PubSubSDK {
    /**
     * Singleton instance.
     */
    private static PubSubSDK instance;

    /**
     * Creates {@link PubSubSDK} instance if none exists, otherwise returns the existing instance.
     * @return PubSubSDK Instance used to work with the Pub/Sub SDK 
     */
    public static PubSubSDK getInstance() {
        if(instance == null) {
            instance = new PubSubSDK();
        }

        return instance;
    }

    /**
     * Singleton constructor
     */
    private PubSubSDK() {
        // No setup needed
    }

    /**
     * Creates a connection with the given project keys, and defaults set for {@link PubSubOptions}.
     *
     * @param projectKeys List of project keys to use for authenticating the connection to establish.
     * @return CompletableFuture<PubSubHandle> Completes with a {@link PubSubHandle} used to make SDK requests.
     */
    public ListenableFuture<PubSubHandle> connect(List<String> projectKeys) {
        return connect(projectKeys, PubSubOptions.DEFAULT_OPTIONS);
    }

    /**
     * Creates a connection with the given project keys, and the given {@link PubSubOptions}.
     * This will use MoreExecutors.directExecutor() for execution of internal threads.
     *
     * @param projectKeys List of project keys to use for authenticating the connection to be establish.
     * @param options     {@link PubSubOptions} to use for the connection.
     * @return CompletableFuture<PubSubHandle> Completes with a {@link PubSubHandle} used to make SDK requests.
     */
    public ListenableFuture<PubSubHandle> connect(List<String> projectKeys, PubSubOptions options) {
        return connect(projectKeys, options, MoreExecutors.directExecutor());
    }

    /**
     * Creates a connection with the given project keys, and the given {@link PubSubOptions}.
     * @param projectKeys List of project keys to use for authenticating the connection to be establish.
     * @param options     {@link PubSubOptions} to use for the connection.
     * @param executor The executor used for internal threads.
     * @return CompletableFuture<PubSubHandle> Completes with a {@link PubSubHandle} used to make SDK requests.
     */
    public ListenableFuture<PubSubHandle> connect(List<String> projectKeys, PubSubOptions options, Executor executor) {
        ListenableFuture<PubSubSocket> connectFuture = PubSubSocket.connectSocket(projectKeys, options, executor);
        Function<PubSubSocket, PubSubHandle> getSessionFunction =
                new Function<PubSubSocket, PubSubHandle>() {
                    public PubSubHandle apply(PubSubSocket pubSubSocket) {
                        return new PubSubHandle(pubSubSocket, 0L);
                    }
                };
        ListenableFuture<PubSubHandle> getHandleFuture = Futures.transform(connectFuture, getSessionFunction);
        return getHandleFuture;
    }
}