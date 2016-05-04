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

    public String getCiidHash() {
        return ciid_hash;
    }

    public String getCampaignName() {
        return campaign_name;
    }

    public Long getCampaignId() {
        return campaign_id == null ? null : campaign_id.longValue();
    }

    public String getEventName() {
        return event_name;
    }

    public String getMessageId() {
        return message_id;
    }

    public String getNotificationMessage() {
        return notification_message;
    }

    public CogsEvent getForwardedEvent() {
        return forwarded_event;
    }
}
