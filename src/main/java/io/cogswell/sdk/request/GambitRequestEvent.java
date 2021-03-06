package io.cogswell.sdk.request;

import android.util.Log;

import io.cogswell.sdk.GambitRequest;
import io.cogswell.sdk.GambitResponse;
import io.cogswell.sdk.Methods;
import io.cogswell.sdk.response.GambitResponseEvent;
import okhttp3.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iganev
 */
public class GambitRequestEvent extends GambitRequest {

    public static class Builder {

        /**
         * Obtained through GambitToolsSDK
         */
        protected final String mClientSalt;

        /**
         * Obtained through GambitToolsSDK
         */
        protected final String mClientSecret;

        /**
         * Obtained through Gambit UI
         */
        protected final String mAccessKey;

        ///
        /**
         * The name of this event (there are no constraints on this other than
         * length).
         */
        protected String mEventName;

        /**
         * The timestamp of the event in ISO-8601 format.
         */
        protected String mTimestamp;

        /**
         * Optional. Supply any desired tags.
         */
        protected ArrayList<String> mTags;

        /**
         * Object containing namepace-specific fields. The names and types of
         * the supplied attributes must match those defined either within the
         * namespace, or defined as core attributes on the customer's account.
         */
        protected JSONObject mAttributes;

        /**
         * The associated campaign ID, if this event is related to a message
         * sent from a campaign.
         */
        protected int mCampaignId = 0;

        /**
         * The namespace for with which this event is associated. The event's
         * attributes must either be defined for the specified namespace, or
         * they must be core attributes defined by the customer owning this
         * namespace.
         */
        protected String mNamespace;

        /**
         * The debug directive
         */
        protected String mDebugDirective;

        /**
         * Debug Directive to forward event as message
         */
        protected boolean mForwardAsMessage = false;

        /**
         * Create request builder with keys obtained through Gambit UI and
         * Gambit Tools SDK
         *
         * @param access_key The access key obtained from Gambit UI
         * @param client_salt The client salt hash obtained from GambitToolsSDK
         * @param client_secret The client secret hash obtained from GambitToolsSDK
         */
        public Builder(String access_key, String client_salt, String client_secret) {
            this.mAccessKey = access_key;
            this.mClientSalt = client_salt;
            this.mClientSecret = client_secret;
        }

        /**
         * Gets the timestamp of the event. Use ISO-8601 format.
         * @return The timestamp of the event.
         */
        public String getTimestamp() {
            return mTimestamp;
        }

        /**
         * Sets the timestamp of the event. Use ISO-8601 format.
         * @param timestamp The timestamp of the event.
         * @return The same instance
         */
        public Builder setTimestamp(String timestamp) {
            this.mTimestamp = timestamp;

            return this;
        }

        /**
         * The name of this event (there are no constraints on this other than
         * length).
         *
         * @param event_name The name of this event
         * @return The same instance
         */
        public Builder setEventName(String event_name) {
            this.mEventName = event_name;

            return this;
        }

        /**
         * The name of this event (there are no constraints on this other than
         * length).
         *
         * @return The name of this event
         */
        public String getEventName() {
            return mEventName;
        }

        /**
         * The associated campaign ID, if this event is related to a message
         * sent from a campaign.
         *
         * @param campaign_id The campaign ID associated with this event; may be null as well
         * @return The same instance
         */
        public Builder setCampaignId(int campaign_id) {
            this.mCampaignId = campaign_id;

            return this;
        }

        /**
         * The associated campaign ID, if this event is related to a message
         * sent from a campaign.
         *
         * @return The campaign ID associated with this event; may be null as well
         */
        public int getCampaignId() {
            return mCampaignId;
        }

        /**
         * The namespace for with which this event is associated. The event's
         * attributes must either be defined for the specified namespace, or
         * they must be core attributes defined by the customer owning this
         * namespace.
         *
         * @param namespace The namespace for with which this event is associated.
         * @return The same instance
         */
        public Builder setNamespace(String namespace) {
            this.mNamespace = namespace;

            return this;
        }

        /**
         * The namespace for with which this event is associated. The event's
         * attributes must either be defined for the specified namespace, or
         * they must be core attributes defined by the customer owning this
         * namespace.
         *
         * @return The namespace for with which this event is associated.
         */
        public String getNamespace() {
            return mNamespace;
        }

        /**
         * Object containing namepace-specific fields. The names and types of
         * the supplied attributes must match those defined either within the
         * namespace, or defined as core attributes on the customer's account.
         *
         * @param attributes
         * @return
         */
        public Builder setAttributes(JSONObject attributes) {
            this.mAttributes = attributes;

            return this;
        }

        /**
         * Object containing namepace-specific fields. The names and types of
         * the supplied attributes must match those defined either within the
         * namespace, or defined as core attributes on the customer's account.
         *
         * @return Namespace specific attributes and their values. Only the CIID attribute(s) is/are mandatory.
         */
        public JSONObject getAttributes() {
            return mAttributes;
        }

        /**
         * Optional. Supply any desired tags.
         *
         * @param tags a list of strings
         * @return The same instance
         */
        public Builder setTags(ArrayList<String> tags) {
            this.mTags = tags;

            return this;
        }

        /**
         * Optional. Supply any desired tags.
         *
         * @return list of tags that have been set previously; may be null as well
         */
        public ArrayList<String> getTags() {
            return mTags;
        }

        /**
         * Obtained through Gambit UI (public key)
         *
         * @return The access key obtained from Gambit UI
         */
        public String getAccessKey() {
            return mAccessKey;
        }

        /**
         * Obtained through Gambit Tools SDK
         *
         * @return The client salt hash
         */
        public String getClientSalt() {
            return mClientSalt;
        }

        /**
         * Obtained through Gambit Tools SDK
         *
         * @return The client secret hash
         */
        public String getClientSecret() {
            return mClientSecret;
        }

        /**
         * Get the debug directive value
         * @return the debug directive value
         */
        public String getDebugDirective() {
            return mDebugDirective;
        }

        /**
         * Set the Debug Directive
         * @param debugDirective echo-as-message or trigger-all-campaigns-no-delays
         * @return The same instance
         */
        public Builder setDebugDirective(String debugDirective) {
            this.mDebugDirective = debugDirective;

            return this;
        }

        /**
         * Set the forward_as_message flag
         * @param flag flag boolean value
         * @return The same instance
         */
        public Builder setForwardAsMessage(boolean flag) {
            this.mForwardAsMessage = flag;

            return this;
        }

        /**
         * Indicate whether the forward_as_message flag is being set to true
         * @return indication of the flag state
         */
        public boolean isForwardAsMessage() {
            return this.mForwardAsMessage;
        }

        /**
         * Build request object
         *
         * @return A {@link GambitRequestEvent} instance
         * @throws java.lang.Exception if validation fails
         */
        public GambitRequestEvent build() throws Exception {
            validate();

            return new GambitRequestEvent(this);
        }

        /**
         * Validate the builder integrity before proceeding with object creation
         * @throws Exception If anything crucial is missing
         */
        protected void validate() throws Exception {
            if (mEventName == null || mEventName.isEmpty()) {
                throw new Exception("Missing mandatory parameter of Builder: event_name");
            }

            if (mAttributes == null) {
                throw new Exception("Missing mandatory parameter of Builder: attributes");
            }

            if (mNamespace == null || mNamespace.isEmpty()) {
                throw new Exception("Missing mandatory parameter of Builder: namespace");
            }

            if (mTimestamp == null || mTimestamp.isEmpty()) {
                throw new Exception("Missing mandatory parameter of Builder: timestamp");
            }
        }
    }

    protected static final String endpoint = "/event";

    /**
     * Generated request body
     */
    protected String mBody;

    /**
     * Obtained through GambitToolsSDK
     */
    protected final String mClientSalt;

    /**
     * Obtained through GambitToolsSDK
     */
    protected final String mClientSecret;

    /**
     * Obtained through Gambit UI
     */
    protected final String mAccessKey;

    ///

    /**
     * The timestamp of the event in ISO-8601 format.
     */
    protected String mTimestamp;

    /**
     * The name of this event (there are no constraints on this other than
     * length).
     */
    protected String mEventName;

    /**
     * Optional. Supply any desired tags.
     */
    protected ArrayList<String> mTags;

    /**
     * Object containing namepace-specific fields. The names and types of the
     * supplied attributes must match those defined either within the namespace,
     * or defined as core attributes on the customer's account.
     */
    protected JSONObject mAttributes;

    /**
     * The associated campaign ID, if this event is related to a message sent
     * from a campaign.
     */
    protected int mCampaignId;

    /**
     * The namespace for with which this event is associated. The event's
     * attributes must either be defined for the specified namespace, or they
     * must be core attributes defined by the customer owning this namespace.
     */
    protected String mNamespace;

    /**
     * The debug directive
     */
    protected String mDebugDirective;

    /**
     * Set to true whenver the debug is set to forward event as message
     */
    protected boolean mForwardAsMessage = false;

    /**
     * Construct the request object using it's own {@link Builder} instance.
     * @param builder The {@link Builder} object
     */
    protected GambitRequestEvent(Builder builder) {
        mAccessKey = builder.getAccessKey();
        mClientSalt = builder.getClientSalt();
        mClientSecret = builder.getClientSecret();

        mTimestamp = builder.getTimestamp();
        mEventName = builder.getEventName();
        mTags = builder.getTags();
        mAttributes = builder.getAttributes();
        mCampaignId = builder.getCampaignId();
        mNamespace = builder.getNamespace();

        mDebugDirective = builder.getDebugDirective();
        mForwardAsMessage = builder.isForwardAsMessage();
    }

    /**
     * Define the HTTP method to use
     * @return POST
     */
    @Override
    protected String getMethod() {
        return "POST";
    }

    /**
     * Build the request URL to execute the API call upon.
     * @return Full request {@link URL}
     */
    @Override
    protected URL getUrl() {
        StringBuilder builder = new StringBuilder();

        builder.append(getBaseUrl());
        builder.append(endpoint);

        URL url = null;

        try {
            url = new URL(builder.toString());
        } catch (MalformedURLException ex) {
            Logger.getLogger(GambitRequestEvent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return url;
    }

    /**
     * Build a JSON according to specification. This is the actual request body.
     * @return JSON string representation of all needed request parameters
     */
    @Override
    protected String getBody() {

        if (mBody == null) {
            JSONObject json = new JSONObject();

            try {
                json.put("client_salt", mClientSalt);

                json.put("access_key", mAccessKey);
                json.put("event_name", mEventName);
                json.put("timestamp", mTimestamp);
                json.put("namespace", mNamespace);
                json.put("attributes", mAttributes);

                if (mCampaignId > 0) {
                    json.put("campaign_id", mCampaignId);
                }

                if (mTags != null && !mTags.isEmpty()) {
                    json.put("tags", mTags);
                }

                if (mDebugDirective != null && !mDebugDirective.isEmpty()) {
                    json.put("debug_directive", mDebugDirective);
                }

                if (mForwardAsMessage) {
                    json.put("forward_as_message", true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Log.d("jsonnew", json.toString());
            mBody = json.toString();
            //Log.d("request mBody", mBody);
        }

        return mBody;
    }

    /**
     * Inject the HMAC-SHA256 hash as a header to the request
     * @param requestBuilder The Request.Builder object that is going to build the API call.
     */
    @Override
    protected void setRequestParams(Request.Builder requestBuilder) {
        try {
            requestBuilder.header("Payload-HMAC", Methods.getHmac(getBody(), mClientSecret));
        } catch (Exception ex) {
            Logger.getLogger(GambitRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Build {@link GambitResponseEvent} instance, containing the result of the request.
     * @param response The RAW HTTP response body as text
     * @param code The RAW HTTP response code as an integer
     * @return An instance of {@link GambitResponseEvent}
     */
    @Override
    protected GambitResponse getResponse(String response, int code) {
        return new GambitResponseEvent(response, code);
    }

    /**
     * Gets the timestamp of the event. Use ISO-8601 format.
     * @return The timestamp of the event.
     */
    public String getTimestamp() {
        return mTimestamp;
    }

    /**
     * Obtained through Gambit UI (public key)
     *
     * @return The access key obtained from Gambit UI
     */
    public String getAccessKey() {
        return mAccessKey;
    }

    /**
     * Obtained through GambitToolsSDK
     *
     * @return The client salt hash obtained from GambitToolsSDK
     */
    public String getClientSalt() {
        return mClientSalt;
    }

    /**
     * Obtained through GambitToolsSDK
     *
     * @return The client secret hash obtained from GambitToolsSDK
     */
    public String getClientSecret() {
        return mClientSecret;
    }

    ///

    /**
     * The name of this event (there are no constraints on this other than
     * length).
     *
     * @return The name of this event
     */
    public String getEventName() {
        return mEventName;
    }

    /**
     * The associated campaign ID, if this event is related to a message sent
     * from a campaign.
     *
     * @return The campaign ID associated with this event; may be null as well
     */
    public int getCampaignId() {
        return mCampaignId;
    }

    /**
     * The namespace for with which this event is associated. The event's
     * attributes must either be defined for the specified namespace, or they
     * must be core attributes defined by the customer owning this namespace.
     *
     * @return The namespace for with which this event is associated.
     */
    public String getNamespace() {
        return mNamespace;
    }

    /**
     * Object containing namepace-specific fields. The names and types of the
     * supplied attributes must match those defined either within the namespace,
     * or defined as core attributes on the customer's account.
     *
     * @return Namespace specific attributes and their values. Only the CIID attribute(s) is/are mandatory.
     */
    public JSONObject getAttributes() {
        return mAttributes;
    }

    /**
     * Optional. Supply any desired tags.
     *
     * @return list of tags that have been set previously; may be null as well
     */
    public ArrayList<String> getTags() {
        return mTags;
    }

    /**
     * Get the debug directive value
     * @return The value of the debug directive
     */
    public String getDebugDirective() {
        return mDebugDirective;
    }

    /**
     * Indicate whether the forward_as_message flag is being set to true
     * @return indication of the flag state
     */
    public boolean isForwardAsMessage() {
        return mForwardAsMessage;
    }
}
