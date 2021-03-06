package io.cogswell.sdk;


import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class GambitRequest implements Callable<GambitResponse> {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    /**
     * Indicate to the server that the request is made by this library. May be useful for debugging.
     */
    protected static final String API_USER_AGENT = "GambitTools SDK for Java SE";
    
    /**
     * The API end point base
     */
    protected static  String mBaseUrl = "https://api.cogswell.io";

    /**
     * Each Request object has it's own Builder, so there's really no need to do anything in this constructor, as the
     * base class is just a container for commonly used methods.
     */

    /**
     * The base URL used to make API calls. This should be refactored so that it supports dev/prod environments and
     * easy, centralized environment switching.
     *
     * @param baseUrl the base URL of the API. All trailing slashes ('/') will be removed before use in routes.
     */
    public static void setBaseUrl(String baseUrl) {
        mBaseUrl = Methods.trimRight(baseUrl, '/');
    }

    public GambitRequest() {
        
    }

    /**
     * Get the base URL for making API calls. Consists of protocol and hostname, including trailing slash
     * @return The API base URL
     */
    protected String getBaseUrl() {
        return mBaseUrl;
    }

    /**
     * Used by the executor thread loop to run the task in background.
     * @return A {@link GambitResponse} inheriting object
     * @throws IOException
     */
    @Override
    public GambitResponse call() throws IOException {
        
        URL url = getUrl();
        String body = getBody();

        RequestBody bodyJson = RequestBody.create(JSON, body);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent", API_USER_AGENT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
         //       .method(getMethod(), bodyJson);

        setRequestParams(requestBuilder); //allow adapter to set more stuff

        if (body.length() > 0)
        {
            requestBuilder.method(getMethod(), bodyJson);
        } else {
            requestBuilder.method(getMethod(), null);
        }

        Request request = requestBuilder.build();
        Response responseObject = client.newCall(request).execute();

        int responseCode = responseObject.code();
        String response = responseObject.body().string();

        return getResponse(response.toString(), responseCode);
    }
    /**
     * Define the HTTP method to be used to make the API call
     * @return POST/GET or whatever you need
     */
    abstract protected String getMethod();
    /**
     * Build the full request URL
     * @return URL to be used to make the API call
     */
    abstract protected URL getUrl();
    /**
     * Build the full request body
     * @return Request body
     */
    abstract protected String getBody();
    /**
     * Use this method to add any additional headers or HTTP connection properties, before making an API call.
     * @param requestBuilder The {@link HttpURLConnection} object that is going to build the API call.
     */
    abstract protected void setRequestParams(Request.Builder requestBuilder);
    /**
     * This method let's the {@link GambitRequest} inheriting object build it's own {@link GambitResponse} object
     * @param response The RAW HTTP response body as text
     * @param code The RAW HTTP response code as an integer
     * @return An {@link GambitResponse} inheriting object
     */
    abstract protected GambitResponse getResponse(String response, int code);

}
