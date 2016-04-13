package io.cogswell.sdk.response;


import android.util.Log;

import io.cogswell.sdk.GambitResponse;

import org.json.JSONException;

public class GambitResponseEvent extends GambitResponse {

    /**
     * Gambit API Event Response Message
     */
    protected String mMessage;

    /**
     * Construct the response object using the raw response body and response code
     * @param response The raw HTTP response body as text
     * @param code The raw HTTP response code as an integer
     */
    public GambitResponseEvent(String response, int code) {
        super(response, code);

        //Log.d("responseCode", String.valueOf(response));
        if (isSuccess()) {
            if (mJson.has("message")) {
                try {
                    mMessage = mJson.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //not good at all
                
                mIsSuccess = false;
                mErrorCode = "UNKNOWN";
                mErrorDetails = "Unknown response: "+response;
            }
        } else if (mJson.has("error_message")) {
            try {
                mMessage = mJson.getString("error_message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get Gambit API Event Response Message
     * @return A server message indicating the request status in human readable format
     */
    public String getMessage() {
        return mMessage;
    }

}
