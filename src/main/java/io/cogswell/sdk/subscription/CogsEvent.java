package io.cogswell.sdk.subscription;

import io.cogswell.sdk.json.JsonNode;
import io.cogswell.sdk.json.JsonObject;

/**
 * Created by jedwards on 5/3/16.
 *
 * Represents the an event forwarded within a message.
 */
public class CogsEvent {
    private String namespace;
    private String event_name;
    private String timestamp;

    private JsonObject attributes;

    public CogsEvent(JsonNode eventJson) {
        namespace = eventJson.str("namespace");
        event_name = eventJson.str("event_name");
        timestamp = eventJson.str("timestamp");

        attributes = (JsonObject) eventJson.obj("attributes");
    }

    public String getNamespace() {
        return namespace;
    }

    public String getEvent_name() {
        return event_name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public JsonObject getAttributes() {
        return attributes;
    }
}
