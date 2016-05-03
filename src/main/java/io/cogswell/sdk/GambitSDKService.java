package io.cogswell.sdk;

import android.net.Uri;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import io.cogswell.sdk.json.Json;
import io.cogswell.sdk.json.JsonNode;
import io.cogswell.sdk.request.GambitRequestEvent;
import io.cogswell.sdk.subscription.CogsMessage;
import io.cogswell.sdk.subscription.CogsSubscription;
import io.cogswell.sdk.subscription.CogsSubscriptionHandler;
import io.cogswell.sdk.subscription.CogsSubscriptionRequest;
import io.cogswell.sdk.subscription.CogsSubscriptionWebSocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class GambitSDKService {
    /**
     * Singleton instance
     */
    protected static GambitSDKService mInstance;

    /**
     * Thread loop
     */
    protected final ExecutorService mExecutor;

    protected ConcurrentHashMap<CogsSubscription, CogsSubscriptionWebSocket> subscriptions = new ConcurrentHashMap<>();

    /**
     * Singleton constructor
     */
    protected GambitSDKService() throws RuntimeException {
        mExecutor = Executors.newCachedThreadPool();

    }

    /**
     * Creates a {@link GambitSDKService} if none previously existed in the VM,
     * otherwise returns the existing {@link GambitSDKService} instance.
     * @return GambitSDKService
     */
    public static GambitSDKService getInstance() throws RuntimeException {
        if (mInstance == null) {
            mInstance = new GambitSDKService();
        }

        return mInstance;
    }
    /**
     * Get the main thread pool
     * @return executor service
     */
    protected ExecutorService getExecutorService() {
        return mExecutor;
    }

    /**
     * Send Gambit Event data
     * @param builder Builder that configures the {@link GambitRequest} inheriting object
     * @return Promised object that inherits {@link GambitResponse}
     * @throws java.lang.Exception
     */
    public Future<GambitResponse> sendGambitEvent(GambitRequestEvent.Builder builder) throws Exception {
        return mExecutor.submit(builder.build());
    }

    /**
     * Establish a new WebSocket via the Cogs GET /push route.
     *
     * @param request the {@link CogsSubscriptionRequest request} detailing the subscription
     * @param handler the {@link CogsSubscriptionHandler handler} for receiving messages from the WebSocket
     */
    public void subscribe(final CogsSubscriptionRequest request, final CogsSubscriptionHandler handler) {
        if (subscriptions.contains(request)) {
            subscriptions.get(request).replaceHandler(handler);
        } else {
            CogsSubscriptionWebSocket oldWebSocket = subscriptions.replace(request.getSubscription(), CogsSubscriptionWebSocket.connect(request, handler));

            if (oldWebSocket != null) {
               oldWebSocket.close();
            }
        }
    }
}
