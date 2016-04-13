package io.cogswell.sdk.push;

import android.util.Log;

import io.cogswell.sdk.GambitResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class GambitResponsePush extends GambitResponse {

    private String message;
    /**
     * Construct the response object using the raw response body and response code
     * @param response The raw HTTP response body as text
     * @param code The raw HTTP response code as an integer
     */
    public GambitResponsePush(String response, int code) {
        super(response, code);

        if (isSuccess()) {
            if (mJson.has("message")) {
                try {
                    message = mJson.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (mJson.has("error_message")) {
                try {
                    message = mJson.getString("error_message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //not good at all

                mIsSuccess = false;
                mErrorCode = "UNKNOWN";
                mErrorDetails = "Unknown response: "+response;
            }
        }
    }

    public String getMessage() {
        return message;
    }
}
