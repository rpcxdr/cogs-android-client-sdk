package io.cogswell.sdk.subscription;

import io.cogswell.sdk.json.JsonNode;
import io.cogswell.sdk.json.JsonObject;

/**
 * Created by jedwards on 5/3/16.
 *
 * Represents a message from Cogs.
 */
public class CogsMessage {
    private String namespace;
    private String ciid_hash;
    private String campaign_name;
    private Number campaign_id;
    private String event_name;
    private String message_id;
    private String notification_message;

    private CogsEvent forwarded_event;

    public CogsMessage(JsonNode json) {
        namespace = json.str("namespace");
        ciid_hash = json.str("ciid_hash");
        campaign_name = json.str("campaign_name");
        campaign_id = json.num("campaign_id");
        event_name = json.str("event_name");
        message_id = json.str("message_id");
        notification_message = json.str("notification_message");

        if (!json.isNull("forwarded_event")) {
            JsonNode eventJson = json.obj("forwarded_event");
            forwarded_event = eventJson.isNull() ? null : new CogsEvent((JsonObject) json.obj("forwarded_event"));
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public String getCiid_hash() {
        return ciid_hash;
    }

    public String getCampaign_name() {
        return campaign_name;
    }

    public Long getCampaign_id() {
        return campaign_id == null ? null : campaign_id.longValue();
    }

    public String getEvent_name() {
        return event_name;
    }

    public String getMessage_id() {
        return message_id;
    }

    public String getNotification_message() {
        return notification_message;
    }

    public CogsEvent getForwarded_event() {
        return forwarded_event;
    }
}
