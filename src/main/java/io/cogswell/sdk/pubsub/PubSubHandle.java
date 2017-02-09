package io.cogswell.sdk.pubsub;

import android.util.Log;

import java.util.concurrent.atomic.AtomicLong;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.base.Function;

import io.cogswell.sdk.pubsub.handlers.*;

/**
 * Users of the SDK receive an instance of this class when connecting to Pub/Sub.
 * All Pub/Sub operations available to users of the SDK are made through an instance of this class.
 */
public class PubSubHandle {
    private AtomicLong sequence;
    private PubSubSocket socket;

    /**
     * Construct a handle that uses the given {@link PubSubSocket} to connect to the Pub/Sub system
     */
    protected PubSubHandle(PubSubSocket socket, Long initialSequenceNumber) {
        this.sequence = new AtomicLong(initialSequenceNumber);
        this.socket = socket;
    }

    private interface JSONReaderFunction<T> {
        T apply(JSONObject json) throws JSONException;
    };

    /**
     * Send a message with the specified params and process the JSON response with the specified reader.
     * A sequence number will be added to the request.  Any errors will resolve the Future with an exception.
     * @param jsonreader converts a JSONObject to an instance of the specified type.
     * @param nameValuePairs Must follow the pattern of a string name followed by a value (String, Object, String, Object, ...)
     * @param isAwaitingServerResponse If true, this call will be registered to resolve when the server responds.  Otherwise it will resolve immediatly with a sequence number.
     * @param <T> The expected result type.
     * @return A future with the result of a call to jsonreader.
     */
    private <T> ListenableFuture<T> sendJSON(final JSONReaderFunction<T> jsonreader, final boolean isAwaitingServerResponse, Object ... nameValuePairs) {
        final SettableFuture<T> outcome = SettableFuture.create();
        long seq = sequence.getAndIncrement();

        try {
            JSONObject request = new JSONObject()
                    .put("seq", seq);

            for(int i = 0; i < nameValuePairs.length-1; i += 2) {
                request.put((String)nameValuePairs[i], nameValuePairs[i+1]);
            }

            ListenableFuture<JSONObject> getSessionFuture = socket.sendRequest(seq, request, isAwaitingServerResponse);

            Futures.addCallback(getSessionFuture, new FutureCallback<JSONObject>() {
                public void onSuccess(JSONObject json) {
                    try {
                        T result = jsonreader.apply(json);
                        outcome.set(result);
                    } catch (JSONException e) {
                        Log.e("COGS-SDK", "Server sent unexpected response" + json.toString(), e);
                        outcome.setException(e);
                    }
                }
                public void onFailure(Throwable t) {
                    Log.e("TEST","Error:",t);
                    outcome.setException(t);
                }
            });
        } catch (JSONException e) {
            // Reject the future if there are any errors.
            outcome.setException(e);
        }
        return outcome;
    }

    /**
     * Request the UUID of the current session/connection with the Pub/Sub system.
     * @return ListenableFuture<UUID> Future that completes with Session UUID on success, and with error otherwise
     */
    public ListenableFuture<UUID> getSessionUuid() {

        return sendJSON(new JSONReaderFunction<UUID>() {
            @Override
            public UUID apply(JSONObject json) throws JSONException {
                return UUID.fromString(json.getString("uuid"));
            }
        }, true, "action", "session-uuid");
    }

    /**
     * Subscribe to the given channel, and process messages from channel with the given {@link PubSubMessageHandler}.
     * THe provided handler cannot be null.
     * @param channel The name of the channel to which to subscribe
     * @param messageHandler The handler which will handle Pub/Sub messages received on the given channel
     * @return ListenableFuture<List<String>> Future completing with list of subscriptions on success, error otherwise
     */
    public ListenableFuture<List<String>> subscribe(String channel, PubSubMessageHandler messageHandler) {
        socket.setMessageHandler(channel, messageHandler);

        return sendJSON(new JSONReaderFunction<List<String>>() {
            @Override
            public List<String> apply(JSONObject json) throws JSONException {
                List<String> channels = Collections.synchronizedList(new LinkedList<String>());
                JSONArray list = json.getJSONArray("channels");

                for(int i = 0; i < list.length(); ++i) {
                    channels.add(list.getString(i));
                }
                return channels;
            }
        }, true, "action", "subscribe", "channel", channel);
    }

    /**
     * Unsubscribes from {@code channel} which stops receipt and handling of messages for {@code channel}.
     *
     * @param channel Name of the channel from which to unsubscribe.
     * @return ListenableFuture<List<String>> Future completing with list of all remaining subscriptions on success.
     */
    public ListenableFuture<List<String>> unsubscribe(final String channel) {
        return sendJSON(new JSONReaderFunction<List<String>>() {
            @Override
            public List<String> apply(JSONObject json) throws JSONException {
                List<String> channels = Collections.synchronizedList(new LinkedList<String>());
                JSONArray list = json.getJSONArray("channels");

                for(int i = 0; i < list.length(); ++i) {
                    channels.add(list.getString(i));
                }

                socket.removeMessageHandler(channel);

                return channels;
            }
        }, true, "action", "unsubscribe", "channel", channel);
    }
    
    /**
     * Unsubscribe from all current channel subscriptions
     * @return ListenableFuture<List<String>> Future completing with list of all unsubscribed channels, error otherwise
     */
    public ListenableFuture<List<String>> unsubscribeAll() {
        return sendJSON(new JSONReaderFunction<List<String>>() {
            @Override
            public List<String> apply(JSONObject json) throws JSONException {
                List<String> channels = Collections.synchronizedList(new LinkedList<String>());
                JSONArray list = json.getJSONArray("channels");

                for(int i = 0; i < list.length(); ++i) {
                    channels.add(list.getString(i));
                }
                return channels;
            }
        }, true, "action", "unsubscribe-all");
    }

    /**
     * Request a list of current subscriptions for the connection
     * @return ListenableFuture<List<String>> Future completing with list of subscriptions on success, error otherwise
     */
    public ListenableFuture<List<String>> listSubscriptions() {
        return sendJSON(new JSONReaderFunction<List<String>>() {
            @Override
            public List<String> apply(JSONObject json) throws JSONException {
                List<String> channels = Collections.synchronizedList(new LinkedList<String>());
                JSONArray list = json.getJSONArray("channels");

                for(int i = 0; i < list.length(); ++i) {
                    channels.add(list.getString(i));
                }
                return channels;
            }
        }, true, "action", "subscriptions");
    }

    /**
     * Publish the given message to the given channel. Note that the ListenableFuture returned by this method
     * indicates success in actually sending the message, but provides no information about whether the message
     * was received. This is unlike the other methods, which return futures with the results from the server.
     * @param channel The name of the channel on which to publish the given message.
     * @param message The actual content of the message to publish on the given channel.
     * //@param handler An error handler that is called if sending fails
     * @return ListenableFuture<Long> Future completed with sequence of message on successful send, error otherwise
     */
    public ListenableFuture<Long> publish(String channel, String message) {
        return sendJSON(new JSONReaderFunction<Long>() {
            @Override
            public Long apply(JSONObject json) throws JSONException {
                Long seq = json.getLong("seq");
                return seq;
            }
        }, false, "action", "pub", "chan", channel, "msg", message, "ack", false);
    }

    /**
     * Publish the given message to the given channel, receiving UUID of published message if publish is successful.
     * @param channel The name of the channel on which to publish the given message.
     * @param message The actual content of the message to publish on the given channel.
     * //@param handler An error handler called if sending fails, or if the ack is not properly received
     * @return ListenableFuture<Long> Future completed with sequence of message on successful send, error otherwise
     */
    public ListenableFuture<UUID> publishWithAck(String channel, String message) {
        return sendJSON(new JSONReaderFunction<UUID>() {
            @Override
                public UUID apply(JSONObject json) throws JSONException {
                UUID uuid = UUID.fromString(json.getString("id"));
                return uuid;
            }
        }, true, "action", "pub", "chan", channel, "msg", message, "ack", true);
    }

    /**
     * Close the connection for good.
     * @return ListenableFuture<List<String>> Future completing with list of subscriptions on success, error otherwise
     */
    public ListenableFuture<List<String>> close() {

        ListenableFuture<List<String>> unsubscribeAllFuture = unsubscribeAll();
        Function<List<String>, List<String>> closeFunction =
                new Function<List<String>, List<String>>() {
                    public List<String> apply(List<String> unsubscribeAllResponse) {
                        socket.close();
                        return unsubscribeAllResponse;
                    }
                };
        ListenableFuture<List<String>> closedFuture = Futures.transform(unsubscribeAllFuture, closeFunction);

        return closedFuture;
    }

    /**
     * Registers a handler to process any published messages received from Cogswell Pub/Sub on any subscribed channels.
     *
     * @param messageHandler The {@link PubSubMessageHandler} that should be registered.
     */
    public void onMessage(PubSubMessageHandler messageHandler) {
        socket.setMessageHandler(messageHandler);
    }

    /**
     * Registers a handler that is called whenever the underlying connection is re-established.
     *
     * @param reconnectHandler The {@link PubSubReconnectHandler} that should be registered.
     */
    public void onReconnect(PubSubReconnectHandler reconnectHandler) {
        socket.setReconnectHandler(reconnectHandler);
    }

    /**
     * Registers a handler to process every raw record (as a JSON-formatted String) received from Cogswell Pub/Sub.
     *
     * @param rawRecordHandler The {@link PubSubRawRecordHandler} that should be registered.
     */
    public void onRawRecord(PubSubRawRecordHandler rawRecordHandler) {
        socket.setRawRecordHandler(rawRecordHandler);
    }

    /**
     * Registers a handler that is called immediately before the underlying connection to Cogswell Pub/Sub is closed.
     *
     * @param closeHandler The {@link PubSubCloseHandler} that should be registered.
     */
    public void onClose(PubSubCloseHandler closeHandler) {
        socket.setCloseHandler(closeHandler);
    }

    /**
     * Registers a handler that is called whenever there is any error with the underlying connection to Cogswell Pub/Sub
     *
     * @param errorHandler The {@link PubSubErrorHandler} that should be registered
     */
    public void onError(PubSubErrorHandler errorHandler) {
        socket.setErrorHandler(errorHandler);
    }

    /**
     * Registers a handler that is called whenever reconnecting the underlying connection to Cogswell Pub/Sub forces a new session
     *
     * @param newSessionHandler The {@link PubSubNewSessionHandler} that should be registered
     */
    public void onNewSession(PubSubNewSessionHandler newSessionHandler) {
        socket.setNewSessionHandler(newSessionHandler);
    }
}