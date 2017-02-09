package io.cogswell.sdk.pubsub;

import android.net.Uri;

import java.util.UUID;

/**
 * This class contains the initialization options when establishing a Pub/Sub connection
 */
public class PubSubOptions {

    /**
     * The url used for connecting to the Pub/Sub service
     */
    Uri uri;

    /**
     * Holds whether a connection should (true) auto-reconnect if the connection is dropped
     */
    private final boolean autoReconnect;

    /**
     * The amount of time, in milliseconds, when a connection attempt should timeout
     */
    private final long connectTimeout;

    /**
     * The session id to be restored, if requested
     */
    private final UUID sessionUuid;

    /**
     * Initializes this PubSubOptions with all default values
     */
    public PubSubOptions () {
        this("wss://api.cogswell.io/pubsub", true, 30000L, null);
    }
    public PubSubOptions (String url) {
        this(url, true, 30000L, null);
    }

    /**
     * Initializes this PubSubOptions with the given options. If any are null, defaults are used.
     * @param url The url with which to connect
     * @param autoReconnect True if the connection should attempt to reconnect when disconnected
     * @param connectTimeout The amount of time, in milliseconds, before a connection should timeout
     * @param sessionUuid The uuid of the session to be restored
     */
    public PubSubOptions (String url, Boolean autoReconnect, long connectTimeout, UUID sessionUuid) {
        this.uri = Uri.parse(url);
        this.autoReconnect = autoReconnect;
        this.connectTimeout = connectTimeout;
        this.sessionUuid = sessionUuid;
    }

    /**
     * Instance of PubSubOptions with all default values
     */
    public static final PubSubOptions DEFAULT_OPTIONS = new PubSubOptions();


    /**
     * Get the url represented in this PubSubOptions for a connection
     * @return String The url represented in this PubSubOptions
     */
    public Uri getUri() {
        return uri;
    }

    /**
     * Get whether these options represent the ability to auto-reconnect.
     * @return boolean True if auto-reconnect was set, false otherwise
     */
    public boolean getAutoReconnect() {
        return autoReconnect;
    }

    /**
     * Get the amount of time, in milliseconds, before a connection attempt should fail
     * @return long The amount of time, in milliseconds, before connection attempt should fail
     */
    public long getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Get the UUID of the session represented in this PubSubOptions that should be re-established
     * @return UUID The uuid of the session requested to be restablished
     */
    public UUID getSessionUuid() {
        return sessionUuid;
    }
}
