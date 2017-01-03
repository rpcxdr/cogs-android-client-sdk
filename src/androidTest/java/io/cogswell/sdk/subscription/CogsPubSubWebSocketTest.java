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
import java.util.Date;

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
    /*
    public void testCreate() throws Exception {
        Log.d("TEST", "~~~~~~~~~~~~~~~~~~~~ hello1");


        String timestamp = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")).format(new Date());
        String namespace = "jae_test_namespace_2";
        String eventName = "e1";
        Integer campaignId = -1;
        JSONObject attributes = new JSONObject();
        attributes.put("jae_test_core_attribute", "test@email.com");
        String accessKey = "a03385c4193beb5edda099772bf5467c";
        String clientSalt = "ca91e3bfa015eaa108acb3c9c5fd7bd01e276ff18fef23600f402b05c70139a7";
        String clientSecret = "7b363e4b364005ae68e693d3532dae3e79fca24b8cebdb4b90e22fa4e6bc206f";

        CogsPubSubWebSocket.setBaseUrl("https://gamqa-api.aviatainc.com");
        CogsSubscriptionRequest sr = CogsSubscriptionRequest.builder()
                .withAccessKey(accessKey)
                .withClientSalt(clientSalt)
                .withClientSecret(clientSecret)
                .withNamespace(namespace)
                .withTopicAttributes(attributes)
                .build();
        CogsPubSubWebSocket ws = CogsPubSubWebSocket.create(sr, null);
        ws.start();

    }
*/
    private Headers buildHeaders() throws Auth.AuthKeyError {

        // Valid QA keys:
        String[] keys = {
            "A-*-*",
            "R-*-*",
            "W-*-*"
        };

        Headers headers = new Headers();
        Auth.PayloadHeaders ph = Auth.socketAuth(keys);
        headers.add("Host" , "gamqa-api.aviatainc.com");
        headers.add("Payload" , ph.payloadBase64);
        headers.add("PayloadHMAC" , ph.payloadHmac);

        return headers;
    }

}