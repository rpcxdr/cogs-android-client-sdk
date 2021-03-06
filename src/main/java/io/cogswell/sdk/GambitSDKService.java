package io.cogswell.sdk;

import android.net.Uri;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import io.cogswell.sdk.json.Json;
import io.cogswell.sdk.json.JsonNode;
import io.cogswell.sdk.request.GambitRequestEvent;
import io.cogswell.sdk.subscription.Callback;
import io.cogswell.sdk.subscription.CogsMessage;
import io.cogswell.sdk.subscription.CogsSubscription;
import io.cogswell.sdk.subscription.CogsSubscriptionHandler;
import io.cogswell.sdk.subscription.CogsSubscriptionRequest;
import io.cogswell.sdk.subscription.CogsSubscriptionWebSocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GambitSDKService {
    /**
     * Singleton instance
     */
    protected static GambitSDKService mInstance;

    /**
     * Thread loop
     */
    protected final ExecutorService mExecutor;
    protected final ScheduledExecutorService mScheduler;

    protected ConcurrentHashMap<CogsSubscription, CogsSubscriptionWebSocket> subscriptions = new ConcurrentHashMap<>();

    /**
     * Singleton constructor
     */
    protected GambitSDKService() throws RuntimeException {
        mExecutor = Executors.newCachedThreadPool();
        mScheduler = Executors.newScheduledThreadPool(2);
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
     * Supplies the singleton executor service.
     *
     * @return the {@link ExecutorService executor service}
     */
    protected ExecutorService getExecutorService() {
        return mExecutor;
    }

    /**
     * Supplies the singleton scheduler service.
     *
     * @return the {@link ScheduledExecutorService scheduler service}
     */
    protected ScheduledExecutorService getSchedulerService() {
        return mScheduler;
    }

    /**
     * Executes an operation in the executor.
     *
     * @param runnable the {@link Runnable} to execute
     */
    public void execute(Runnable runnable) {
        getExecutorService().execute(runnable);
    }

    /**
     * Execuates an operation in the scheduler after a delay.
     *
     * @param delay the delay before execution
     * @param unit the {@link TimeUnit units} of the delay
     * @param runnable the {@link Runnable} to execute
     */
    public void schedule(long delay, TimeUnit unit, Runnable runnable) {
        getSchedulerService().schedule(runnable, delay, unit);
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
        CogsSubscriptionWebSocket oldWebSocket = subscriptions.get(request);

        if (oldWebSocket != null) {
            Log.i("Cogs-SDK", "Replacing handler for existing WebSocket.");
            oldWebSocket.replaceHandler(handler);
        } else {
            Log.i("Cogs-SDK", "Creating new WebSocket.");
            CogsSubscriptionWebSocket webSocket = CogsSubscriptionWebSocket.create(request, handler);
            subscriptions.put(request.getSubscription(), webSocket);
            webSocket.start();
        }
    }

    /**
     * Terminate a subscription.
     *
     * @param subscription the subscription to terminate
     * @param callback the {@link Callback} to invoke once the subscription is terminated.
     */
    public void unsubscribe(final CogsSubscription subscription, final Callback<Boolean> callback) {
        CogsSubscriptionWebSocket ws = subscriptions.remove(subscription);
        ws.stop(callback);
    }

    /**
     * Get a set containing all established subscriptions.
     *
     * @return the {@link Set} of subscriptions
     */
    public Set<CogsSubscription> getSubscriptions() {
        return subscriptions.keySet();
    }
}
