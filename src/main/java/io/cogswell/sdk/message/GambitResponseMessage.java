package io.cogswell.sdk.message;

import io.cogswell.sdk.GambitResponse;


public class GambitResponseMessage extends GambitResponse {

    /**
     * Construct the response object using the raw response body and response code
     * @param response The raw HTTP response body as text
     * @param code The raw HTTP response code as an integer
     */
    public GambitResponseMessage(String response, int code) {
        super(response, code);

    }


}
