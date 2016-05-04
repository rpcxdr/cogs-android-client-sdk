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
import java.util.concurrent.atomic.AtomicBoolean;

import io.cogswell.sdk.Methods;
import io.cogswell.sdk.exceptions.CogsSubscriptionException;
import io.cogswell.sdk.json.Json;
import io.cogswell.sdk.json.JsonNode;

/**
 * Created by jedwards on 5/3/16.
 *
 * Ties a WebSocket to a subscription.
 */
public class CogsSubscriptionWebSocket implements CogsSubscriptionHandler {
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

    private CogsSubscription subscription;
    private CogsSubscriptionHandler handler;

    private WebSocket webSocket;
    private AtomicBoolean closer = new AtomicBoolean(true);

    private CogsSubscriptionWebSocket(CogsSubscription subscription) {
        this.subscription = subscription;
    }

    private CogsSubscriptionHandler currentHandler() {
        return handler == null ? stubHandler : handler;
    }

    @Override public void error(Throwable t) {
        currentHandler().error(t);
    }

    @Override public void connected() {
        currentHandler().connected();
    }

    @Override public void message(CogsMessage message) {
        currentHandler().message(message);
    }

    @Override public void closed(Throwable t) {
        currentHandler().closed(t);
    }

    @Override public void replaced() {
        ;
    }

    /**
     * Close the WebSocket permanently. Since this is a clean shutdown, no attempts will
     * be made to automatically reconnect the underlying WebSocket.
     */
    public void close() {
        if (!closer.getAndSet(false))
            return;

        WebSocket ws = webSocket;
        CogsSubscriptionHandler h = handler;

        webSocket = null;
        handler = null;

        try {
            if (ws != null) {
                ws.close();
            }
        } finally {
            h.closed(null);
        }
    }

    public void replaceHandler(CogsSubscriptionHandler handler) {
        CogsSubscriptionHandler oldHandler = this.handler;
        this.handler = handler;

        if (oldHandler != null) {
            oldHandler.replaced();
        }
    }

    protected void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public static void setBaseUrl(String baseUrl) {
        CogsSubscriptionWebSocket.baseUrl = baseUrl;
    }

    private static Uri getPushUri() {
        return Uri.parse(baseUrl + "/push");
    }

    private void reconnect() {
        
    }

    public static CogsSubscriptionWebSocket connect(CogsSubscriptionRequest request, CogsSubscriptionHandler handler) {
        Log.i("Cogs-SDK", "Connecting push subscription WebSocket to namespace '" +
                request.getNamespace() + "' topic '" + request.getTopicAttributes() + "'.");

        Headers headers = new Headers();
        JSONObject payload = new JSONObject();

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

        headers.add("Host", baseHost);
        headers.add("Json-Base64" , b64Payload);
        headers.add("Payload-HMAC" , hmac);

        Log.i("Cogs-SDK", "Payload JSON: " + jsonPayload);
        Log.i("Cogs-SDK", "Json-Base64: " + b64Payload);
        Log.i("Cogs-SDK", "Payload-HMAC" + hmac);

        AsyncHttpRequest httpRequest = new AsyncHttpRequest(getPushUri(), "GET", headers);

        final CogsSubscriptionWebSocket ws = new CogsSubscriptionWebSocket(request.getSubscription());
        ws.replaceHandler(handler);

        try {
            AsyncHttpClient.getDefaultInstance().websocket(httpRequest, "cogs", new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception error, WebSocket webSocket) {
                    webSocket.isOpen();

                    if (error != null) {
                        Log.e("Cogs-SDK", "Error on subscription WebSocket connect.", error);
                        ws.error(error);
                    } else {
                        ws.setWebSocket(webSocket);

                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            @Override
                            public void onStringAvailable(String str) {
                                JsonNode json = Json.parse(str);

                                if (!json.isNull()) {
                                    CogsMessage message = new CogsMessage(json);
                                    try {
                                        ws.ackMessage(message.getMessageId());
                                    } finally {
                                        ws.message(message);
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

                                ws.closed(error);
                            }
                        });

                        ws.connected();
                    }
                }
            });
        } catch (Throwable t) {
            Log.e("Cogs-SDK", "Error connecting Subscription WebSocket.", t);
            throw new CogsSubscriptionException("Error connecting Subscription WebSocket.", t);
        }

        return ws;
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
}
