package io.cogswell.sdk.pubsub;

public class PubSubOptions {
    String url;
    boolean autoReconnect;
    long connectTimeout;

    public PubSubOptions (String url) {
        this(url, true, 30000);
    }

    public PubSubOptions (String url, boolean autoReconnect) {
        this(url, autoReconnect, 30000);
    }

    public PubSubOptions (String url, Boolean autoReconnect, long connectTimeout) {
        this.url = url;
        this.autoReconnect = autoReconnect;
        this.connectTimeout = connectTimeout;
    }

    public static PubSubOptions defaultOptions = new PubSubOptions("wss://api.cogswell.io/pubsub");
}
