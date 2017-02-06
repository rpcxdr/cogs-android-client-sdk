package io.cogswell.sdk.pubsub;

//import javax.websocket.*;

import android.util.Log;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

//import java.util.concurrent.CompletionException;
//import java.util.concurrent.CompletableFuture;
import org.json.JSONObject;

import java.util.List;

import io.cogswell.sdk.Auth;
import io.cogswell.sdk.pubsub.PubSubHandle;

/**
 * The main class that all SDK users will use to work with the Pub/Sub SDK. 
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
     * Creates a connection with the given project keys, and the defaults set for the {@link PubSubOptions}
     * @param projectKeys The list of requested keys for the connection to be established
     * @return CompletableFuture<PubSubHandle> future that will contain {@PubSubHandle} used for making SDK requests 
     */
    public ListenableFuture<PubSubHandle> connect(List<String> projectKeys) {
        return connect(projectKeys, PubSubOptions.DEFAULT_OPTIONS);
    }

    /**
     * Creates a connection with the given project keys, and the given {@link PubSubOptions}
     * @param projectKeys The list of requested keys for the connection to be established
     * @param options The {@link PubSubOptions} to use for the connection to be established
     * @return CompletableFuture<PubSubHandle> future that will contain {@PubSubHandle} used for making SDK requests 
     */
    public ListenableFuture<PubSubHandle> connect(List<String> projectKeys, PubSubOptions options) {
        try {
            ListenableFuture<PubSubSocket> connectFuture = PubSubSocket.connectSocket(projectKeys, options);
            Function<PubSubSocket, PubSubHandle> getSessionFunction =
                new Function<PubSubSocket, PubSubHandle>() {
                    public PubSubHandle apply(PubSubSocket pubSubSocket) {
                        return new PubSubHandle(pubSubSocket, 0L);
                    }
                };
            ListenableFuture<PubSubHandle> getHandleFuture = Futures.transform(connectFuture, getSessionFunction);
            return getHandleFuture;
        } catch (Auth.AuthKeyError e) {
            SettableFuture<PubSubHandle> failedFuture = SettableFuture.create();
            failedFuture.setException(e);
            return failedFuture;
        }
    }
}