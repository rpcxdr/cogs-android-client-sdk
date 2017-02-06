package io.cogswell.sdk.pubsub;

import android.util.Log;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.List;

import java.io.IOException;

import java.net.URI;

import org.json.JSONException;
import org.json.JSONObject;

import io.cogswell.sdk.Auth;
import io.cogswell.sdk.json.Json;
import io.cogswell.sdk.json.JsonNode;
import io.cogswell.sdk.pubsub.exceptions.*;
import io.cogswell.sdk.pubsub.handlers.*;
import io.cogswell.sdk.subscription.CogsMessage;

/**
 * PubSubSocket is used to wrap the logic of {@link com.koushikdutta.async.http.WebSocket} websockets.
 * It also servers the purpose of tracking
 * and routing incoming and outgoing message to and from the Pub/Sub server.
 */
public class PubSubSocket implements WebSocket.StringCallback, AsyncHttpClient.WebSocketConnectCallback, CompletedCallback
{
    private static Executor executor = MoreExecutors.directExecutor();

    public static ListenableFuture<PubSubSocket> connectSocket(List<String> projectKeys)
    {
        try {
            return connectSocket(projectKeys, PubSubOptions.DEFAULT_OPTIONS);
        } catch (Auth.AuthKeyError e) {
            SettableFuture<PubSubSocket> result = SettableFuture.create();
            result.setException(e);
            return result;
        }
    }

    public static ListenableFuture<PubSubSocket> connectSocket(List<String> projectKeys, PubSubOptions options)
            throws Auth.AuthKeyError
    {
        PubSubSocket socket = new PubSubSocket(projectKeys, options);
        return socket.connect();
    }

    /**
     * This is the shortest default delay that the socket will wait between reconnects
     * Current it is set to 5 seconds = 5 s * 1000 ms/s = 5000 ms.
     */
    private static final long DEFAULT_RECONNECT_DELAY = 5000L; // 5 seconds

    /**
     * This is the longest the socket will wait between reconnects before giving up.
     * Currently it is set to 2 minutes = 2 min * 60 s/min * 1000 ms/s = 120000 ms.
     */
    private static final long MAX_RECONNECT_DELAY = 120000L; // 2 minutes

    /**
     * The keys used when creating this PubSubSocket.
     */
    private List<String> projectKeys;

    /**
     * The {@link PubSubOptions} used when creating this PubSubSocket.
     */
    private PubSubOptions options;

    /**
     * The {@link WebSocket} that represents this PubSubSocket as a websocket Endpoint connection.
     */
    WebSocket webSocketSession;

    /**
     * Tracks whether this socket is actually connected to the Pub/Sub server
     */
    private AtomicBoolean isConnected;

    /**
     * Tracks whether this socket is actually connected to the Pub/Sub server
     */
    private AtomicBoolean isSetupInProgress;

    /**
     * This future is resolved when the socket is connected.
     */
    private SettableFuture<PubSubSocket> setupFuture;

    /**
     * Holds whether to actually reconnect (based on length of delay, and whether close was chosen)
     */
    private AtomicBoolean autoReconnect;

    /**
     * Holds the next delay to wait when an attempted reconnect fails
     */
    private AtomicLong autoReconnectDelay;

    /**
     * Maps each outstanding request to the server by their sequence number 
     * with their associated {@link com.google.common.util.concurrent.ListenableFuture}
     */
    private Map<Long, SettableFuture<JSONObject>> outstanding;

    /**
     * Maps the channel subscriptions of this PubSubSocket with the specific message handlers given for those channels. 
     */
    private Map<String, PubSubMessageHandler> msgHandlers;

    /**
     * Handler called whenever server generates a new session for this connection
     */
    private PubSubNewSessionHandler newSessionHandler;

    /**
     * Handler called whenever this connection must reconnect for some reason
     */
    private PubSubReconnectHandler reconnectHandler;

    /**
     * Handler called whenever any raw string json message is received from the server
     */
    private PubSubRawRecordHandler rawRecordHandler;

    /**
     * Handler called as general message handler whenever published messages are received from server 
     */
    private PubSubMessageHandler generalMsgHandler;

    /**
     * Handler called whenever an error having to do with this connection is encountered
     */
    private PubSubErrorHandler errorHandler;

    /**
     * Handler called whenever an error having to do with a server response is encountered.
     * TODO: Update this to comply with https://aviatainc.atlassian.net/browse/PUB-210.
     */
    private PubSubErrorHandler responseErrorHandler = new PubSubErrorHandler(){
        @Override
        public void onError(Throwable error, Long sequence, String channel) {
            if (errorHandler!=null) {
                errorHandler.onError(error, sequence, channel);
            }
        }
    };

    /**
     * Handler called whenever this connection closes
     */
    private PubSubCloseHandler closeHandler; 

    /**
     * Creates a minimal PubSubSocket, used for testing purposes
     */
    protected PubSubSocket() {
        this(null, PubSubOptions.DEFAULT_OPTIONS);
        this.autoReconnectDelay = new AtomicLong(DEFAULT_RECONNECT_DELAY);
    }

    /**
     * Creates a minimal PubSubSocket, using the provided server as the connect to which to send messages
     * Used for testing purposes
     * @param webSocketSession The server to which to send messages
     */
    protected PubSubSocket(WebSocket webSocketSession) {
        this();
        this.webSocketSession = webSocketSession;
    }

    /**
     * Creates a connection to the Pub/Sub server given project keys and {@link PubSubOptions}
     * @param projectKeys The configuration requested for the connection represented by this PubSubSocket
     * @param options The options requested for the connection represented by this PubSubSocket
     * @throws IOException
     */
    public PubSubSocket(List<String> projectKeys, PubSubOptions options)
    {
        this.isSetupInProgress = new AtomicBoolean(false);
        this.setupFuture = null;


        this.projectKeys = projectKeys;
        this.msgHandlers = Collections.synchronizedMap(new Hashtable<String, PubSubMessageHandler>());
        this.outstanding = Collections.synchronizedMap(new Hashtable<Long, SettableFuture<JSONObject>>());

        this.autoReconnectDelay = new AtomicLong(options.getConnectTimeout());
        this.autoReconnect = new AtomicBoolean(options.getAutoReconnect());
        this.isConnected = new AtomicBoolean(false);
        this.options = options;
    }

    /**
     * Closes the connection represented by this PubSubSocket
     */
    public void close() 
    {
        autoReconnect.set(false);
        webSocketSession.close();
    }

    /**
     * Sends the given request, represented by the {@link org.json.JSONObject}, to the server and maps the
     * eventual result to be stored in a {@link com.google.common.util.concurrent.ListenableFuture} with the sequence
     * number of the message.
     * @param sequence Sequence number of the message
     * @param json The request to send to the Pub/Sub server
     * @param isAwaitingServerResponse if true, the retrurned future will resolve when the server sends a response.  If false, will return immedialty with the sequence number in the JSON, with the following structure {"seq":seq}.
     *
     * @return CompletableFuture<JSONObject> future that will contain server response to given request, or the sequence number if we are not waiting on the server.
     */
    protected ListenableFuture<JSONObject> sendRequest(long sequence, JSONObject json, boolean isAwaitingServerResponse) {
        SettableFuture<JSONObject> result = SettableFuture.create();

        try {
            if (isAwaitingServerResponse) {
                // If we're awaiting a server response, store this sequence number.
                outstanding.put(sequence, result);
            }

            String jsonString = json.toString();
            Log.d("Cogs-SDK", "Sending JSON: " + jsonString);
            webSocketSession.send(json.toString());

            if (!isAwaitingServerResponse) {
                // If we're not awaiting a server response, resolve this promise immediately with the sequence number.
                JSONObject seqResponse = new JSONObject();
                seqResponse.put("seq", sequence);
                result.set(seqResponse);
            }
        } catch (Throwable t) {
            // Reject the promise if there is any exception.
            result.setException(t);
        }

        return result;
    }

    /**
     * Initiates the connection the the Pub/Sub server with the configuration for this PubSubSocket.
     * @throws Auth.AuthKeyError if the auth was an invalid format.
     */
    private ListenableFuture<PubSubSocket> connect() throws Auth.AuthKeyError {

        // Only allow one reconnect at a time.  Do nothing if we are already reconnecting.
        if (isSetupInProgress.compareAndSet(false, true)) {
            setupFuture = SettableFuture.create();
            Headers headers = new Headers();
            Auth.PayloadHeaders ph = Auth.socketAuth(projectKeys);
            headers.add("Host", "gamqa-api.aviatainc.com");
            headers.add("Payload", ph.payloadBase64);
            headers.add("PayloadHMAC", ph.payloadHmac);

            AsyncHttpRequest httpRequest = new AsyncHttpRequest(options.uri, "GET", headers);

            // This calls onCompleted(Exception error, WebSocket webSocket) on when complete:
            AsyncHttpClient.getDefaultInstance().websocket(httpRequest, "websocket", this);             /*
            AsyncHttpClient.getDefaultInstance().websocket(httpRequest, "websocket", new AsyncHttpClient.WebSocketConnectCallback() {
            */
        }
        return setupFuture;
    }

    /**
     * Attempts to reconnects a socket that has been dropped for any reason other than intentionally and cleanly disconnecting.
     * @return ListenableFuture<PubSubSocket> future that completes successfully when connected, with an error otherwise
     * @throws Auth.AuthKeyError Contains the auth cause of being unable to reconnect, if such occurs
     */
    private ListenableFuture<PubSubSocket> reconnect() throws Auth.AuthKeyError {
        ListenableFuture<PubSubSocket> connectFuture = connect();
        AsyncFunction<PubSubSocket, PubSubSocket> reconnectHandlerFunction =
                new AsyncFunction<PubSubSocket, PubSubSocket>() {
                    public ListenableFuture<PubSubSocket> apply(PubSubSocket connectResponse) {
                        Log.d("Cogs-SDK", "reconnect() connection established.");
                        reconnectHandler.onReconnect();

                        //if (options.getAutoReconnect()) {
                        //    SettableFuture<PubSubSocket> new PubSubHandle(PubSubSocket.this, -1L).getSessionUuid();
                        //} else {
                            SettableFuture<PubSubSocket> result = SettableFuture.create();
                            result.set(connectResponse);
                            return result;
                        //}

                    }
                };
        ListenableFuture<PubSubSocket> reconnectHandlerFuture = Futures.transformAsync(connectFuture, reconnectHandlerFunction, executor);

        return reconnectHandlerFuture;
    }

    /*
    private CompletableFuture<Void> reconnect() {
        return connect()
            .thenAcceptAsync((invalid) -> {
                if(reconnectHandler != null) {
                    reconnectHandler.onReconnect();
                }
            })
            .exceptionally((error) -> {
                if(errorHandler != null) {
                    errorHandler.onError(error, null, null);
                }

                throw new CompletionException(error);
            });
    }*/

    ///////////////////// EXTENDING ENDPOINT AND IMPLEMENTING MESSAGE_HANDLER ///////////////////// 

    /**
     * Called immediately after establishing the connection represented by this PubSubSocket
     * @param error The configuration used to establish this PubSubSocket
     * @param webSocket The session that has just been activated by this PubSubSocket
     */
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
            Log.d("Cogs-SDK", "Successfully connected.");
            PubSubSocket.this.webSocketSession = webSocket;

            webSocket.setStringCallback(PubSubSocket.this);
            webSocket.setClosedCallback(PubSubSocket.this);
            webSocket.setEndCallback(new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    Log.d("Cogs-SDK", "setEndCallback got exception:", ex);
                }
            });

            //currentHandler().connected();
            setupFuture.set(PubSubSocket.this);
            isConnected.set(true);

            if(autoReconnect.get()) {

            }
        }
    }

    /*
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        isConnected.set(true);
        session.setMessageHandler(this);
    }*/

    /**
     * Called immediately before closing the connection represented by this PubSubSocket
     * @param closeReason The reason for closing this PubSubSocket
     */
    @Override
    public void onCompleted(Exception closeReason) {
        long previousDelay;
        long minimumDelay;
        long nextDelay;

        isConnected.set(false);

        if(closeHandler != null) {
            closeHandler.onClose(closeReason);
        }
/*
        if (closeReason != null) {
            Log.e("Cogs-SDK", "Error caused WebSocket to close.", closeReason);
        } else {
            Log.i("Cogs-SDK", "WebSocket closed without error.");
        }
*/

        if(options.getAutoReconnect() == true) {
            Log.d("Cogs-SDK", "Lost connection.  Attempting reconnect.", closeReason);
            reconnectRetry(0);
            /*
            new Thread(new Runnable() {
                @Override
                public void run() {
                    do {
                        previousDelay = autoReconnectDelay.get();
                        minimumDelay = Math.max(DEFAULT_RECONNECT_DELAY, previousDelay);
                        nextDelay = Math.min(minimumDelay, MAX_RECONNECT_DELAY) * 2;
                        autoReconnectDelay.set(nextDelay);
                    } while(isConnected.get() != true && autoReconnectDelay.get() < MAX_RECONNECT_DELAY);

                }
            }).start();
            /*
            do {
                try {
                    reconnect();
                }
                catch(Exception e) {
                    Utils.setTimeout( new Runnable() {
                        public void run(){
                            try {
                                PubSubSocket.this.reconnect();
                            } catch (Auth.AuthKeyError ake) {
                                Log.e("Cogs-SDK","Auth error when reconnecting", ake);
                            }
                        }
                    }, autoReconnectDelay.get());

                    previousDelay = autoReconnectDelay.get();
                    minimumDelay = Math.max(DEFAULT_RECONNECT_DELAY, previousDelay);
                    nextDelay = Math.min(minimumDelay, MAX_RECONNECT_DELAY) * 2;
                    autoReconnectDelay.set(nextDelay);
                }
            } while(isConnected.get() != true && autoReconnectDelay.get() < MAX_RECONNECT_DELAY);
            /*
            Log.d("Cogs-SDK", "Lost connection.  Attempting reconnect.", closeReason);
            do {
                try {
                    reconnect();
                }
                catch(Exception e) {
                    Utils.setTimeout( new Runnable() {
                        public void run(){
                            try {
                                PubSubSocket.this.reconnect();
                            } catch (Auth.AuthKeyError ake) {
                                Log.e("Cogs-SDK","Auth error when reconnecting", ake);
                            }
                        }
                    }, autoReconnectDelay.get());

                    previousDelay = autoReconnectDelay.get();
                    minimumDelay = Math.max(DEFAULT_RECONNECT_DELAY, previousDelay);
                    nextDelay = Math.min(minimumDelay, MAX_RECONNECT_DELAY) * 2;
                    autoReconnectDelay.set(nextDelay);
                }
            } while(isConnected.get() != true && autoReconnectDelay.get() < MAX_RECONNECT_DELAY);
            */
        }
    }
    private void reconnectRetry(final long msUntilNextRetry) {
        Log.d("Cogs-SDK", "Attempting reconnect in "+msUntilNextRetry+"ms.");
        Utils.setTimeout( new Runnable() {
            public void run() {
                try {
                    ListenableFuture<PubSubSocket> connectFuture = reconnect();

                    Futures.addCallback(connectFuture, new FutureCallback<PubSubSocket>() {
                        public void onSuccess(PubSubSocket psh) {
                            Log.d("Cogs-SDK", "Successfully reconnected after lost connection.");
                        }

                        public void onFailure(Throwable error) {

                            long minimumDelay = Math.max(DEFAULT_RECONNECT_DELAY, msUntilNextRetry);
                            long nextDelay = Math.min(minimumDelay * 2, MAX_RECONNECT_DELAY);
                            // TODO: PUB-238
                            //if (nextDelay<MAX_RECONNECT_DELAY) {
                                reconnectRetry(nextDelay);
                            //} else {
                            //    Log.e("Cogs-SDK", "Max reconnect attempts reached.  Stopping retries.");
                            //}
                        }
                    }, executor);
                } catch (Auth.AuthKeyError ake) {
                    Log.e("Cogs-SDK", "Auth error when reconnecting", ake);
                }
            }
        }, msUntilNextRetry);
    }
    /**
     * Called whenever the connection represented by this PubSubSocket produces errors
     * @param session The session that has produced an error
     * @param throwable The error that was thrown involving the session
     *//*
    @Override
    public void onError(Session session, Throwable throwable) {
        if(errorHandler != null) {
            errorHandler.onError(throwable, null, null);
        }
    }*/

    /**
     * Called when receiving messages from the remote endpoint (Pub/Sub server). 
     * The method proprogates Pub/Sub messages to appropriate channels when it receives them,
     * and completes outstanding futures when receiving response to other requests.
     * @param message The message received from the remote endpoint
     */
    @Override
    public void onStringAvailable(String message) {
        Log.d("Cogs-SDK","Received JSON: "+message);

        if(rawRecordHandler != null) {
            rawRecordHandler.onRawRecord(message);
        }

        // TODO: validate format of message received from server, if invalid call error

        try {
            JSONObject json = new JSONObject(message);

            if (json.getString("action").equals("msg")) {
                String id = json.getString("id");
                String msg = json.getString("msg");
                String time = json.getString("time");
                String chan = json.getString("chan");

                PubSubMessageRecord record = new PubSubMessageRecord(chan, msg, time, id);
                PubSubMessageHandler handler = msgHandlers.get(chan);

                handler.onMessage(record);

                if (generalMsgHandler != null) {
                    generalMsgHandler.onMessage(record);
                }
            } else if (!json.has("seq")) {
                // This should never happen.  Log it and send to the handler, if specified.
                PubSubException error = new PubSubException("Expected sequence number missing: "+message);
                Log.e("Cogs-SDK","Expected sequence number missing.", error);
                if (responseErrorHandler!=null) {
                    responseErrorHandler.onError(error, null, null);
                }
            } else {
                long seq = json.getLong("seq");
                SettableFuture<JSONObject> responseFuture = outstanding.get(seq);
                if (responseFuture !=null) {
                    if (json.getInt("code") != 200) {
                        responseFuture.setException(new PubSubException("Received an error from the server:" + message));
                    } else {
                        responseFuture.set(json);
                    }
                    outstanding.remove(seq);
                } else {
                    // All responses with a seq should have a future waiting for it.
                    Log.w("Cogs-SDK","Recieved a message with a sequence number, but no client handler is listening for it: "+message);
                }
            }
        } catch (JSONException e) {
            // This should never happen.  Log it and send to the handler, if specified.
            PubSubException error = new PubSubException("Could not parse response from server: "+message, e);
            Log.e("Cogs-SDK","Could not parse response from server.", error);
            if (responseErrorHandler!=null) {
                responseErrorHandler.onError(error, null, null);
            }
        }
    }

    /**
     * Registers a handler to call whenever a new session is generated by the server
     * @param handler The handler to call
     */
    public void addNewSessionHandler(PubSubNewSessionHandler handler) {
        newSessionHandler = handler;
    }

    /**
     * Registers a handler that will be called any time the underlying socket must be reconnected
     * @param handler The handler to register for the reconnects
     */
    public void addReconnectHandler(PubSubReconnectHandler handler) {
        reconnectHandler = handler;
    }

    /**
     * Register a handler to call whenever a raw record (string json) is received from the server.
     * @param handler The handler to register
     */
    public void addRawRecordHandler(PubSubRawRecordHandler handler) {
        rawRecordHandler = handler;
    }

    /**
     * Registers a handler to call if there are failures working with the underlying socket
     * @param handler The handler to register
     */
    public void addErrorHandler(PubSubErrorHandler handler) {
        errorHandler = handler;
    }

    /**
     * Registers a handler to call if there are failures working with server responses.
     * @param handler The handler to register
     */
    private void addResponseErrorHandler(PubSubErrorHandler handler) {
        responseErrorHandler = handler;
    }

    /**
     * Register a handler to call whenever the underlying socket is actually closed.
     * @param handler The handler to register
     */
    public void addCloseHandler(PubSubCloseHandler handler) {
        closeHandler = handler;
    }

    /**
     * Registers a general handler that receives and handles message from all channels.
     * @param handler The handler to be registered
     */
    public void setMessageHandler(PubSubMessageHandler handler) {
        generalMsgHandler = handler;
    }

    /**
     * Associates a {@link PubSubMessageHandler} to call for message received from the given channel.
     * @param channel The channel with which to associate the given handler
     * @param handler The {@link PubSubMessageHandler} that will be called for message from the given channel.
     */
    public void setMessageHandler(String channel, PubSubMessageHandler handler) {
        msgHandlers.put(channel, handler);
    }

    /**
     * Disassociates the current {@link PubSubMessageHandler}, if any, with the given channel.
     * @param channel The channel from which to remove the handler
     */
    public void removeMessageHandler(String channel) {
        msgHandlers.remove(channel);
    }

}