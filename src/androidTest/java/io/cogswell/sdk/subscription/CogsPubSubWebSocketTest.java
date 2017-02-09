package io.cogswell.sdk.subscription;

import android.net.Uri;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.cogswell.sdk.Auth;
import io.cogswell.sdk.Methods;
import io.cogswell.sdk.exceptions.CogsSubscriptionException;
import io.cogswell.sdk.json.Json;
import io.cogswell.sdk.json.JsonNode;

public class CogsPubSubWebSocketTest extends TestCase {
    public void testWS() throws Exception {

        Headers headers = buildHeaders();
        AsyncHttpRequest httpRequest = new AsyncHttpRequest(Uri.parse("https://gamqa-api.aviatainc.com/pubsub"), "GET", headers);

        AsyncHttpClient.getDefaultInstance().websocket(httpRequest, "websocket", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception error, WebSocket webSocket) {
                if (error != null) {
                    Log.e("Cogs-SDK", "Error on subscription WebSocket connect.", error);
                } else if (webSocket == null) {
                    Log.e("Cogs-SDK", "Error on subscription WebSocket connect - could not connect.");
                } else {
                    //setWebSocket(webSocket);

                    webSocket.setStringCallback(new WebSocket.StringCallback() {
                        @Override
                        public void onStringAvailable(String str) {
                            JsonNode json = Json.parse(str);

                            if (!json.isNull()) {
                                CogsMessage message = new CogsMessage(json);
                                try {
                                    Log.d("Cogs-SDK", "message.getMessageId():"+message.getMessageId());
                                    //ackMessage(message.getMessageId());
                                } finally {
                                    Log.d("Cogs-SDK", "message"+message);
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
                        }
                    });

                    //currentHandler().connected();
                }
            }
        });
        Thread.sleep(1000);
    }

    private Headers buildHeaders() throws Auth.AuthKeyError {

        // Valid QA keys:
        List<String> keys = new ArrayList<>();
        keys.add("A-*-*");
        keys.add("R-*-*");
        keys.add("W-*-*");

        Headers headers = new Headers();
        Auth.PayloadHeaders ph = Auth.socketAuth(keys, null);
        headers.add("Host" , "gamqa-api.aviatainc.com");
        headers.add("Payload" , ph.payloadBase64);
        headers.add("PayloadHMAC" , ph.payloadHmac);

        return headers;
    }

}