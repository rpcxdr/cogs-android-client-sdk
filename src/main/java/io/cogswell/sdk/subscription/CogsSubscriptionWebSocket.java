package io.cogswell.sdk.subscription;

import android.net.Uri;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import io.cogswell.sdk.Methods;
import io.cogswell.sdk.exceptions.CogsBuilderException;
import io.cogswell.sdk.json.Json;
import io.cogswell.sdk.json.JsonNode;

/**
 * Created by jedwards on 5/3/16.
 *
 * Ties a WebSocket to a subscription.
 */
public class CogsSubscriptionWebSocket implements CogsSubscriptionHandler {
    private static String baseUrl = "wss://api.cogswell.io";

    private CogsSubscription subscription;
    private CogsSubscriptionHandler handler;

    private WebSocket webSocket;

    private CogsSubscriptionWebSocket(CogsSubscription subscription) {
        this.subscription = subscription;
    }

    private CogsSubscriptionHandler currentHandler() {
        return handler;
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

    @Override public void closed() {
        currentHandler().closed();
    }

    @Override public void replaced() {
        ;
    }

    public static void setBaseUrl(String baseUrl) {
        CogsSubscriptionWebSocket.baseUrl = baseUrl;
    }

    private static Uri getPushUri() {
        return Uri.parse(baseUrl + "/push");
    }

    public static CogsSubscriptionWebSocket connect(CogsSubscriptionRequest request, CogsSubscriptionHandler handler) {
        Headers headers = new Headers();
        JSONObject payload = new JSONObject();

        try {
            payload.put("access_key", request.getAccessKey());
            payload.put("client_salt", request.getClientSalt());
            payload.put("timestamp", Methods.isoNow());
            payload.put("namespace", request.getNamespace());
            payload.put("attributes", request.getTopicAttributes());
        } catch (JSONException e) {
            throw new CogsBuilderException("Error assembling WebSocket auth headers.", e);
        }

        String jsonPayload = payload.toString();
        byte[] rawPayload = jsonPayload.getBytes(Methods.UTF_8);
        String b64Payload = Methods._printBase64Binary(rawPayload);

        String hmac;

        try {
            hmac = Methods.getHmac(jsonPayload, request.getClientSecret());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw new CogsBuilderException("Error signing auth payload header.", e);
        }

        headers.add("Json-Base64" , b64Payload);
        headers.add("Payload-HMAC" , hmac);

        AsyncHttpRequest httpRequest = new AsyncHttpRequest(getPushUri(), "GET", headers);

        final CogsSubscriptionWebSocket ws = new CogsSubscriptionWebSocket(request.getSubscription());
        ws.replaceHandler(handler);

        AsyncHttpClient.getDefaultInstance().websocket(httpRequest, "cogs", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ws.error(ex);
                } else {
                    webSocket.setStringCallback(new WebSocket.StringCallback() {
                        @Override
                        public void onStringAvailable(String str) {
                            JsonNode json = Json.parse(str);

                            if (!json.isNull()) {
                                CogsMessage message = new CogsMessage(json);
                                ws.message(message);
                            }
                        }
                    });

                    webSocket.setClosedCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            ws.closed();
                        }
                    });

                    ws.connected();
                }
            }
        });

        return ws;
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }
    }

    public void replaceHandler(CogsSubscriptionHandler handler) {
        CogsSubscriptionHandler oldHandler = this.handler;
        this.handler = handler;

        if (oldHandler != null) {
            oldHandler.replaced();
        }
    }
}
