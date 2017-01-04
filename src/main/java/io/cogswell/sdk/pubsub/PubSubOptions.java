package io.cogswell.sdk.pubsub;

import android.net.Uri;

public class PubSubOptions {
    boolean autoReconnect;
    long connectTimeout;
    Uri uri;

    public PubSubOptions (String url) {
        this(url, true, 30000);
    }

    public PubSubOptions (String url, boolean autoReconnect) {
        this(url, autoReconnect, 30000);
    }

    public PubSubOptions (String url, Boolean autoReconnect, long connectTimeout) {
        this.uri = Uri.parse(url);
        this.autoReconnect = autoReconnect;
        this.connectTimeout = connectTimeout;
    }

    public static PubSubOptions defaultOptions = new PubSubOptions("wss://api.cogswell.io/pubsub");
}
