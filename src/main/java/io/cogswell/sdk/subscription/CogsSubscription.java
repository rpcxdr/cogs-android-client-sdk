package io.cogswell.sdk.subscription;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;

import io.cogswell.sdk.json.JsonNode;
import io.cogswell.sdk.json.JsonObject;

/**
 * Created by jedwards on 5/3/16.
 *
 * This class uniquely identifies a subscription (namespace + topic attributes).
 */
public class CogsSubscription {
    private String namespace;
    private JSONObject topicAttributes;
    private JsonObject topicAttributesJson;

    public CogsSubscription(String namespace, JSONObject topicAttributes) {
        this.namespace = namespace;
        this.topicAttributes = topicAttributes;
        topicAttributesJson = topicAttributes == null ? null : new JsonObject(topicAttributes);
    }

    public String getNamespace() {
        return namespace;
    }

    public JSONObject getTopicAttributes() {
       return topicAttributes;
    }

    public JsonNode getTopicAttributesJson() {
        return new JsonObject(topicAttributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CogsSubscription that = (CogsSubscription) o;

        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null)
            return false;

        if (topicAttributes != null && that.topicAttributes != null) {
            if (topicAttributes.length() != that.topicAttributes.length())
                return false;

            Iterator<String> keyIter = topicAttributes.keys();

            while (keyIter.hasNext()) {
                String key = keyIter.next();

                Object thisValue;
                try {
                    thisValue = topicAttributes.get(key);
                } catch (JSONException e) {
                    thisValue = null;
                }

                Object thatValue;
                try {
                    thatValue = that.topicAttributes.get(key);
                } catch (JSONException e) {
                    thatValue = null;
                }


                if (thisValue != null) {
                  if (!thisValue.equals(thatValue)) {
                      return false;
                  }
                }
                else if (thatValue != null) {
                  if (!thatValue.equals(thatValue)) {
                      return false;
                  }
                }
            }

            return true;
        }
        else
            return topicAttributes == that.topicAttributes;
    }

    @Override
    public int hashCode() {
        int result = namespace != null ? namespace.hashCode() : 0;

        if (topicAttributes != null) {
                Iterator<String> keyIter = topicAttributes.keys();

            while (keyIter.hasNext()) {
                try {
                    String key = keyIter.next();
                    result = 32 * result + key.hashCode();

                    Object value = topicAttributes.get(key);
                    result = 32 * result + value.hashCode();
                } catch (JSONException e) {
                    ;
                }
            }
        }
        result = 31 * result + (topicAttributes != null ? topicAttributes.hashCode() : 0);

        return result;
    }
}
