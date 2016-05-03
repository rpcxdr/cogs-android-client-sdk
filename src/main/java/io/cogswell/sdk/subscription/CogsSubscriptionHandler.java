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
     * @param t the {@link Throwable error}
     */
    public void error(Throwable t);

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
     */
    public void closed();

    /**
     * Called with this handler is replaced by another.
     */
    public void replaced();
}
