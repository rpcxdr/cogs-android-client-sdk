package io.cogswell.sdk.subscription;

import android.net.Uri;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.cogswell.sdk.GambitSDKService;
import io.cogswell.sdk.Methods;
import io.cogswell.sdk.exceptions.CogsSubscriptionException;
import io.cogswell.sdk.json.Json;
import io.cogswell.sdk.json.JsonNode;

/**
 * Created by jedwards on 5/3/16.
 *
 * Ties a WebSocket to a subscription.
 */
public class CogsPubSubWebSocket {
    private static String baseHost = "api.cogswell.io";
    private static String baseUrl = "https://" + baseHost;

    // Easier than null checks... (see currentHandler() method)
    private static CogsSubscriptionHandler stubHandler = new CogsSubscriptionHandler() {
        @Override public void error(Throwable error) { }
        @Override public void connected() { }
        @Override public void message(CogsMessage message) { }
        @Override public void closed(Throwable error) { }
        @Override public void replaced() { }
    };

    private CogsSubscriptionRequest request;
    private CogsSubscriptionHandler handler;

    private WebSocket webSocket;

    private AtomicBoolean starter = new AtomicBoolean(true);
    private AtomicBoolean stopper = new AtomicBoolean(true);

    private boolean started = false;
    private boolean stopped = false;
    private boolean reconnecting = false;

    private CogsPubSubWebSocket(CogsSubscriptionRequest request) {
        this.request = request;
    }

    private CogsSubscriptionHandler currentHandler() {
        return handler == null ? stubHandler : handler;
    }

    public void replaceHandler(CogsSubscriptionHandler handler) {
        CogsSubscriptionHandler oldHandler = this.handler;
        this.handler = handler;

        if (oldHandler != null) {
            oldHandler.replaced();
        }
    }

    private void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public static void setBaseUrl(String baseUrl) {
        CogsPubSubWebSocket.baseUrl = baseUrl;
    }

    private static Uri getPushUri() {
        return Uri.parse(baseUrl + "/push");
    }

    private void reconnect() {
        if (!reconnecting) {
            doReconnect();
        }
    }

    private synchronized void doReconnect() {
        try {
            reconnecting = true;

            Log.i("Cogs-SDK", "Connecting push subscription WebSocket to namespace '" +
                    request.getNamespace() + "' topic '" + request.getTopicAttributes() + "'.");

            Headers headers = buildHeaders(baseHost, request);
            AsyncHttpRequest httpRequest = new AsyncHttpRequest(getPushUri(), "GET", headers);

            AsyncHttpClient.getDefaultInstance().websocket(httpRequest, "websocket", new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception error, WebSocket webSocket) {
                    if (error != null) {
                        Log.e("Cogs-SDK", "Error on subscription WebSocket connect.", error);
                        currentHandler().error(error);
                    } else if (webSocket == null) {
                        Log.e("Cogs-SDK", "Error on subscription WebSocket connect - could not connect.");
                        currentHandler().error(new Exception("Error on subscription WebSocket connect - could not connect."));
                    } else {
                        setWebSocket(webSocket);

                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            @Override
                            public void onStringAvailable(String str) {
                                JsonNode json = Json.parse(str);

                                if (!json.isNull()) {
                                    CogsMessage message = new CogsMessage(json);
                                    try {
                                        ackMessage(message.getMessageId());
                                    } finally {
                                        currentHandler().message(message);
                                    }
                                }
                            }
                        });

                        webSocket.setClosedCallback(new CompletedCallback() {
                            @Override
                            public void onCompleted(Exception error) {
                                if (error != null) {
                                    Log.e("Cogs-SDK", "Error caused WebSocket to close.", error);
                                } else {
                                    Log.i("Cogs-SDK", "WebSocket closed without error.");
                                }

                                if (stopped) {
                                    currentHandler().closed(error);
                                } else {
                                    delayedReconnect();
                                }
                            }
                        });

                        currentHandler().connected();
                    }
                }
            });
        } catch (Throwable t) {
            Log.e("Cogs-SDK", "Error connecting Subscription WebSocket.", t);
            delayedReconnect();
        } finally {
            reconnecting = false;
        }
    }

    private void delayedReconnect() {
        Log.i("Cogs-SDK", "Reconnecting in 5 seconds.");

        GambitSDKService.getInstance().schedule(5000, TimeUnit.MILLISECONDS, new Runnable() {
            public void run() {
                reconnect();
            }
        });
    }

    private void ackMessage(String messageId) {
        WebSocket ws = webSocket;

        if (ws != null) {
            JSONObject json = new JSONObject();
            try {
                json.put("event", "message-received");
                json.put("message_id", messageId);
                ws.send(json.toString());
            } catch (Throwable error) {
                Log.e("Cogs-SDK", "Error sending message acknowledgement", error);
            }
        }
    }

    /**
     * Starts this subscription WebSocket. If the inner WebSocket terminates for a reason other
     * than a call to stop(), it will be replaced automatically.
     */
    public void start() {
        if (!starter.getAndSet(false))
            return;

        started = true;
        reconnect();
    }

    /**
     * Stops this subscription WebSocket.
     *
     * @param callback the {@link Callback} to invoke once this subscription WebSocket has been stopped.
     */
    public void stop(Callback<Boolean> callback) {
        if (!stopper.getAndSet(false)) {
            callback.call(false);
            return;
        }

        stopped = true;
        WebSocket ws = webSocket;
        CogsSubscriptionHandler h = handler;

        webSocket = null;
        handler = null;

        try {
            if (ws != null) {
                ws.close();
            }
        } finally {
            try {
                h.closed(null);
            } finally {
                callback.call(true);
            }
        }
    }

    private Headers buildHeaders(String host, CogsSubscriptionRequest request) {
        JSONObject payload = new JSONObject();

        Headers headers = new Headers();

        try {
            payload.put("access_key", request.getAccessKey());
            payload.put("client_salt", request.getClientSalt());
            payload.put("timestamp", Methods.isoNow());
            payload.put("namespace", request.getNamespace());
            payload.put("attributes", request.getTopicAttributes());
        } catch (JSONException e) {
            throw new CogsSubscriptionException("Error assembling WebSocket auth headers.", e);
        }

        String jsonPayload = payload.toString();
        byte[] rawPayload = jsonPayload.getBytes(Methods.UTF_8);
        String b64Payload = Methods._printBase64Binary(rawPayload);

        String hmac;

        try {
            hmac = Methods.getHmac(jsonPayload, request.getClientSecret());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw new CogsSubscriptionException("Error signing auth payload header.", e);
        }

        headers.add("Host", host);
        headers.add("Json-Base64" , b64Payload);
        headers.add("Payload-HMAC" , hmac);

        Log.i("Cogs-SDK", "Payload JSON: " + jsonPayload);
        Log.i("Cogs-SDK", "Json-Base64: " + b64Payload);
        Log.i("Cogs-SDK", "Payload-HMAC" + hmac);

        return headers;
    }

    /**
     * Creates a new instance.
     *
     * @param request the {@link CogsSubscriptionRequest} detailing auth and the subscription
     * @param handler the {@link CogsSubscriptionHandler} to handle subscription events
     *
     * @return the new {@link CogsPubSubWebSocket websocket}
     */
    public static CogsPubSubWebSocket create(CogsSubscriptionRequest request, CogsSubscriptionHandler handler) {
        CogsPubSubWebSocket ws = new CogsPubSubWebSocket(request);
        ws.replaceHandler(handler);

        return ws;
    }
}
