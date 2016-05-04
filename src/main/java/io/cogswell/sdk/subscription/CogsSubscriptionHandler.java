package io.cogswell.sdk.subscription;

/**
 * Created by jedwards on 5/3/16.
 *
 * Handler for events relating to Cogs push WebSockets.
 */
public interface CogsSubscriptionHandler {
    /**
     * Called when an error occurs establishing the WebSocket.
     *
     * @param error the {@link Throwable error} which occurred
     */
    public void error(Throwable error);

    /**
     * Called when the WebSocket is established.
     */
    public void connected();

    /**
     * Called when a new message is received.
     *
     * @param message the parsed {@link CogsMessage message}
     */
    public void message(CogsMessage message);

    /**
     * Called when the WebSocket is closed.
     *
     * @param error the {@link Throwable error} which resulted in the closing of the WebSocket,
     *              or <tt>null</tt> if there was no error associated with the closing of the WebSocket
     */
    public void closed(Throwable error);

    /**
     * Called with this handler is replaced by another.
     */
    public void replaced();
}
