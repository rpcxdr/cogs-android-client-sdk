package io.cogswell.sdk.pubsub;

import android.net.Uri;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import java.util.concurrent.atomic.AtomicBoolean;

import io.cogswell.sdk.Auth;
import io.cogswell.sdk.json.Json;
import io.cogswell.sdk.json.JsonNode;
import io.cogswell.sdk.subscription.CogsMessage;

public class PubSubHandle {
    private String[] keys;
    private PubSubOptions options;
    SettableFuture<PubSubHandle> setupFuture;
    AtomicBoolean isSetupInProgress;
    WebSocket webSocket;

    /**
     * This class can only be instantiated using it's factory method.  This allows the construction
     * to be completed asynchronously.
     */
    private PubSubHandle(){
        isSetupInProgress = new AtomicBoolean(false);
        setupFuture = SettableFuture.create();
;   };

    public static ListenableFuture<PubSubHandle> connect(String[] keys){
        return connect(keys, null);
    }

    public static ListenableFuture<PubSubHandle> connect(String[] keys, PubSubOptions options){
        PubSubHandle h = new PubSubHandle();
        try {
            h.keys = keys;
            h.options = (options==null) ? PubSubOptions.defaultOptions : options;
            h.reconnect();
        } catch (Exception e) {
            h.setupFuture.setException(e);
        }
        return h.setupFuture;
    }

    private void reconnect() throws Auth.AuthKeyError {
        // Only allow one reconnect at a time.  Do nothing if we are already reconnecting.
        if (isSetupInProgress.compareAndSet(false, true)) {
            Headers headers = new Headers();
            Auth.PayloadHeaders ph = Auth.socketAuth(keys);
            headers.add("Host", "gamqa-api.aviatainc.com");
            headers.add("Payload", ph.payloadBase64);
            headers.add("PayloadHMAC", ph.payloadHmac);

            AsyncHttpRequest httpRequest = new AsyncHttpRequest(options.uri, "GET", headers);

            AsyncHttpClient.getDefaultInstance().websocket(httpRequest, "websocket", new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception error, WebSocket webSocket) {
                    isSetupInProgress.set(false);
                    if (error != null) {
                        Log.e("Cogs-SDK", "Error on subscription WebSocket connect.", error);
                        setupFuture.setException(error);
                    } else if (webSocket == null) {
                        Log.e("Cogs-SDK", "Error on subscription WebSocket connect - could not connect.");
                        setupFuture.setException(new Exception("Error on subscription WebSocket connect - could not connect - null websocket."));
                    } else {
                        //setWebSocket(webSocket);
                        setupFuture.set(PubSubHandle.this);
                        PubSubHandle.this.webSocket = webSocket;

                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            @Override
                            public void onStringAvailable(String str) {
                                JsonNode json = Json.parse(str);

                                if (!json.isNull()) {
                                    CogsMessage message = new CogsMessage(json);
                                    try {
                                        Log.d("Cogs-SDK", "message.getMessageId():" + message.getMessageId());
                                        //ackMessage(message.getMessageId());
                                    } finally {
                                        Log.d("Cogs-SDK", "message" + message);
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
                                /*
                                // TODO: GAM-2659: Implement reconnect strategy.
                                try {
                                    // Scheduler.schedule(reconnectDelay, reconnect)
                                    // PubSubHandle.this.reconnect();
                                } catch (Auth.AuthKeyError authError) {
                                    Log.e("Cogs-SDK", "Error reconnecting: Auth error:", authError);
                                }*/
                            }
                        });

                        //currentHandler().connected();
                    }
                }
            });
        }

        /*
        if (!done.isMarked) {
            val sock = new PubSubSocket(keys, options)

            sock.onClose(cause => {
                if (!done.isMarked) {
                    Scheduler.schedule(reconnectDelay)(reconnect)
                }

                Try(closeHandler.foreach(_(cause))) match {
                    case Failure(error) => errorHandler.foreach(_(error, None, None))
                    case Success(_) =>
                }
            })

            socket = Some(sock)

            sock.connect() onComplete {
                case Success(_) => setupPromise.success(this)
                case Failure(error) => {
                    setupPromise.failure(error)
                    done.mark
                }
            }
        }*/
    }

}
