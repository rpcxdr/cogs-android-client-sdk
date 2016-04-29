package io.cogswell.sdk.push;

import android.util.Log;

import io.cogswell.sdk.GambitRequest;
import io.cogswell.sdk.GambitResponse;
import io.cogswell.sdk.Methods;
import io.cogswell.sdk.response.GambitResponseEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TimeZone;

/**
 *
 * @author iganev
 */
public class GambitRequestPush extends GambitRequest {

    public static class Builder {

        /**
         * Obtained through Gambit UI
         */
        protected final String mAccessKey;

        /**
         * Obtained through GambitToolsSDK
         */
        protected final String mClientSalt;

        /**
         * Obtained through GambitToolsSDK
         */
        protected final String mClientSecret;
        /**
         * Obtained through push notification
         */
        protected String mUDID;
        /**
         * Obtained through aviatainc
         */
        protected String mPlatform;
        /**
         * Obtained through aviatainc
         */
        protected String mPlatformAppID;
        /**
         * Obtained through aviatainc
         */
        protected String mEnviornment;

        /**
         * Method that should be called, register or unregister
         */
        protected String mMethodName;


        /**
         * Object containing namepace-specific fields. The names and types of
         * the supplied attributes must match those defined either within the
         * namespace, or defined as core attributes on the customer's account.
         */
        protected JSONObject mAttributes;
        /**
         * The namespace for with which this request is associated. The
         * attributes must either be defined for the specified namespace, or
         * they must be core attributes defined by the customer owning this
         * namespace.
         */
        protected String mNamespace;
        /**
         * Create request builder with keys obtained through Gambit UI and
         * Gambit Tools SDK
         *
         * @param access_key
         * @param client_salt
         * @param client_secret
         */
        public Builder(String access_key, String client_salt, String client_secret) {
            this.mAccessKey = access_key;
            this.mClientSalt = client_salt;
            this.mClientSecret = client_secret;
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
         * Object containing namepace-specific fields. The names and types of
         * the supplied attributes must match those defined either within the
         * namespace, or defined as core attributes on the customer's account.
         *
         * @param attributes Namespace specific attributes and their values. Only the CIID attribute(s) is/are mandatory.
         * @return The same instance
         */
        public Builder setAttributes(JSONObject attributes) {
            this.mAttributes = attributes;

            return this;
        }
        /**
         * Obtained through GambitToolsSDK
         *
         * @return The client secret hash obtained from GambitToolsSDK
         */
        public String getClientSecret() {
            return mClientSecret;
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
         * Obtained through GambitToolsSDK
         *
         * @return The UUID token obtained from GambitToolsSDK with push notification
         */
        public String getUDID() {
            return mUDID;
        }

        /**
         * Object containing the token
         *
         * @param udid the token returnet from the push notification
         * @return The same instance
         */
        public Builder setUDID(String udid) {
            this.mUDID = udid;

            return this;
        }

        public String getPlatform() {
            return mPlatform;
        }

        /**
         * Obtained from aviatainc
         *
         * @param platform
         * @return
         */
        public Builder setPlatform(String platform) {
            this.mPlatform = platform;

            return this;
        }

        public String getPlatformAppID() {
            return mPlatformAppID;
        }
        /**
         * Obtained from aviatainc
         *
         * @param platform_app_id
         * @return
         */
        public Builder setPlatformAppID(String platform_app_id) {
            this.mPlatformAppID = platform_app_id;

            return this;
        }

        public String getEnviornment() {
            return mEnviornment;
        }
        /**
         * Name of the method - register or unregister
         *
         * @param method
         * @return
         */
        public Builder setMethodName(String method) {
            this.mMethodName = method;

            return this;
        }

        public String getMethodName() {
            return mMethodName;
        }
        /**
         * Obtained from aviatainc
         *
         * @param enviornment
         * @return
         */
        public Builder setEnviornment(String enviornment) {
            this.mEnviornment = enviornment;

            return this;
        }
        /**
         * The namespace for with which this event is associated. The
         * attributes must either be defined for the specified namespace, or
         * they must be core attributes defined by the customer owning this
         * namespace.
         *
         * @param namespace
         * @return
         */
        public Builder setNamespace(String namespace) {
            this.mNamespace = namespace;

            return this;
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
         * Obtained through Gambit UI (public key)
         *
         * @return The access key obtained from Gambit UI
         */
        public String getAccessKey() {
            return mAccessKey;
        }


        /**
         * Build request object
         *
         * @return A {@link GambitRequestPush} instance
         * @throws java.lang.Exception if validation fails
         */
        public GambitRequestPush build() throws Exception {
            return new GambitRequestPush(this);
        }
    }

    public static final String register = "/register_push";
    public static final String unregister = "/unregister_push";
    /**
     * Generated request body
     */
    protected String mBody;

    /**
     * Obtained through Gambit UI
     */
    protected final String mAccessKey;

    /**
     * Obtained through GambitToolsSDK
     */
    protected final String mClientSalt;

    /**
     * Obtained through GambitToolsSDK
     */
    protected final String mClientSecret;


    protected final Builder mBuilder;

    /**
     * The payload used to start the push service
     */
    protected String mPayload;
    /**
     * The payload data used to start the push service
     */
    protected String mPayloadData;

    /**
     * Obtained through GambitToolsSDK
     */
    protected String mSignature;

    protected String mMethodName;
    /**
     * The namespace for with which to obtain schema for.
     */
    protected String mNamespace;

    /**
     * Construct the request object using it's own {@link Builder} instance.
     * @param builder The {@link Builder} object
     */
    protected GambitRequestPush(Builder builder) {
        mAccessKey = builder.getAccessKey();
        mClientSalt = builder.getClientSalt();
        mClientSecret = builder.getClientSecret();
        mMethodName = builder.getMethodName();
        mBuilder = builder;
        ///

        JSONObject payload = new JSONObject();

        try {
            payload.put("udid", builder.getUDID());
            payload.put("platform", builder.getPlatform());
            payload.put("environment", builder.getEnviornment());
            payload.put("platform_app_id", builder.getPlatformAppID());
            payload.put("access_key", builder.getAccessKey());

            payload.put("client_salt", builder.getClientSalt());

            if (builder.getNamespace() != null && !builder.getNamespace().isEmpty()) {
                payload.put("namespace", builder.getNamespace());
            }

            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
            df.setTimeZone(tz);
            String timestamp = df.format(new Date());

            payload.put("timestamp", timestamp);

            if (builder.getAttributes() != null) {
                payload.put("attributes", builder.getAttributes());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String payloadData = payload.toString();
        mPayloadData = payloadData;

        try {
            mSignature = GambitRequest.getHmac(payloadData, builder.getClientSecret());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        mPayload = Methods._printBase64Binary(payloadData.getBytes());



    }

    /**
     * Define the HTTP method to use
     * @return POST
     */
    @Override
    protected String getMethod() {
        if (mMethodName.equals(register)) {
            return "POST";
        } else {
            return "DELETE";
        }
    }

    /**
     * Build the request URL to execute the API call upon.
     * @return Full request {@link URL}
     */
    @Override
    protected URL getUrl() {
        StringBuilder builder = new StringBuilder();
        builder.append(getBaseUrl());
        if (mMethodName.equals(register)) {
            builder.append(register);
        } else {
            builder.append(unregister);
        }

        URL url = null;

        try {
            url = new URL(builder.toString());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        //Log.d("builder.toString()", builder.toString());
        return url;
    }

    /**
     * Build a JSON according to specification. This is the actual request body.
     * @return JSON string representation of all needed request parameters
     */
    @Override
    protected String getBody() {

        if (mPayloadData == null) {
            mPayloadData = "";
        }

        return mPayloadData;
    }

    /**
     * Inject the HMAC-SHA256 hash as a header to the request
     * @param connection The {@link HttpURLConnection} object that is going to execute the API call.
     */
    @Override
    protected void setRequestParams(HttpURLConnection connection) {
        try {
            //Log.d("mPayloadData", mPayloadData);
            //Log.d("mSignature", String.valueOf(mSignature));
            //Log.d("mPayload", String.valueOf(mPayload));
            //connection.setRequestProperty("JSON-Base64", mPayload);
            connection.setRequestProperty("Payload-HMAC", mSignature);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Build {@link GambitResponsePush} instance, containing the result of the request.
     * @param response The RAW HTTP response body as text
     * @param code The RAW HTTP response code as an integer
     * @return An instance of {@link GambitResponsePush}
     */
    @Override
    protected GambitResponse getResponse(String response, int code) {
        return new GambitResponsePush(response, code);
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


}
