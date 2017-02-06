package io.cogswell.sdk.pubsub;

import android.util.Log;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import io.cogswell.sdk.pubsub.handlers.PubSubCloseHandler;
import io.cogswell.sdk.pubsub.handlers.PubSubReconnectHandler;

public class PubSubSocketTest extends TestCase {

    private Executor executor = MoreExecutors.directExecutor();
    private static int asyncTimeoutSeconds = 100;

    List<String> keys = new ArrayList<String>();
    {
        keys.add("A-*-*");
        keys.add("R-*-*");
        keys.add("W-*-*");
    }

    public void testConnect() throws Exception {
        final Map<String,Object> responses = new HashMap<String, Object>();

        final CountDownLatch signal = new CountDownLatch(1);
        Object result = null;


        ListenableFuture<PubSubSocket> connectFuture = PubSubSocket.connectSocket(keys, new PubSubOptions("https://gamqa-api.aviatainc.com/pubsub"));

        assertNotNull(connectFuture);
        Futures.addCallback(connectFuture, new FutureCallback<PubSubSocket>() {
            public void onSuccess(PubSubSocket pubsubSocket) {
                responses.put("pubsubSocket", pubsubSocket);
                signal.countDown();
            }
            public void onFailure(Throwable error) {
                Log.e("TEST","Error:",error);
                responses.put("pubsubSocket", error);
                signal.countDown();
            }
        }, executor);

        signal.await(asyncTimeoutSeconds, TimeUnit.SECONDS);

        assertTrue(responses.get("pubsubSocket") instanceof PubSubSocket);
    }

    public void testGetSessionSuccessful() throws Exception {
        final Map<String,Object> responses = new HashMap<String, Object>();
        final CountDownLatch signal = new CountDownLatch(1);
        final long seqNum = 123;
        final JSONObject getSessionRequest = new JSONObject()
                .put("seq", seqNum)
                .put("action", "session-uuid");

        //testHandle.getSessionUuid()
        ListenableFuture<PubSubSocket> connectFuture = PubSubSocket.connectSocket(keys, new PubSubOptions("https://gamqa-api.aviatainc.com/pubsub"));
        AsyncFunction<PubSubSocket, JSONObject> getSessionFunction =
                new AsyncFunction<PubSubSocket, JSONObject>() {
                    public ListenableFuture<JSONObject> apply(PubSubSocket pubsubSocket) {
                        responses.put("pubsubSocket", pubsubSocket);
                        //return null;
                        return pubsubSocket.sendRequest(seqNum, getSessionRequest, true);
                    }
                };
        ListenableFuture<JSONObject> getSessionFuture = Futures.transformAsync(connectFuture, getSessionFunction, executor);

        Futures.addCallback(getSessionFuture, new FutureCallback<JSONObject>() {
            public void onSuccess(JSONObject getSessionResponse) {
                responses.put("getSessionResponse", getSessionResponse);
                signal.countDown();
            }
            public void onFailure(Throwable error) {
                Log.e("TEST","Error:",error);
                responses.put("getSessionResponse", error);
                signal.countDown();
            }
        }, executor);

        signal.await(asyncTimeoutSeconds, TimeUnit.SECONDS);

        assertTrue(responses.get("getSessionResponse") instanceof JSONObject);
        assertTrue(responses.get("getSessionResponse").toString().contains("uuid"));
    }

    public void testReconnect() throws Exception {
        final Map<String,Object> responses = new HashMap<String, Object>();
        final CountDownLatch signalClosed = new CountDownLatch(1);
        final CountDownLatch signalReconnected = new CountDownLatch(1);
        final long seqNum = 123;
        final Exception expectedConnectionException = new Exception();
        final JSONObject getSessionRequest = new JSONObject()
                .put("seq", seqNum)
                .put("action", "session-uuid");
        final PubSubCloseHandler closeHandler = new PubSubCloseHandler() {
            @Override
            public void onClose(Throwable error) {
                responses.put("onCloseError", error);
                signalClosed.countDown();
            }
        };
        final PubSubReconnectHandler reconnectHandler = new PubSubReconnectHandler() {
            @Override
            public void onReconnect() {
                responses.put("onReconnect", true);
                signalReconnected.countDown();
            }
        };

        PubSubOptions pubSubOptions = new PubSubOptions("https://gamqa-api.aviatainc.com/pubsub", true, 30000L, null);
        ListenableFuture<PubSubSocket> connectFuture = PubSubSocket.connectSocket(keys, pubSubOptions);

        Futures.addCallback(connectFuture, new FutureCallback<PubSubSocket>() {
            public void onSuccess(PubSubSocket pubsubSocket) {
                responses.put("pubsubSocket", pubsubSocket);
                pubsubSocket.addCloseHandler(closeHandler);
                pubsubSocket.addReconnectHandler(reconnectHandler);

                // Force an unplanned closing.
                pubsubSocket.onCompleted(expectedConnectionException);
            }
            public void onFailure(Throwable error) {
                Log.e("TEST","Error:",error);
                responses.put("pubsubSocket", error);
            }
        }, executor);

        // Wait until the closeHandler function is called.
        signalClosed.await(asyncTimeoutSeconds, TimeUnit.SECONDS);

        assertTrue(responses.get("onCloseError") instanceof Throwable);
        assertEquals(expectedConnectionException, responses.get("onCloseError"));

        // Wait until the reconnectHandler function is called.
        signalReconnected.await(asyncTimeoutSeconds, TimeUnit.SECONDS);

        assertEquals(true, responses.get("onReconnect"));
    }

}