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

public abstract class GambitRequest implements Callable<GambitResponse> {

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
     */
    public static void setBaseUrl(String baseUrl) {
        mBaseUrl = baseUrl;
    }
    public GambitRequest() {
        
    }
    /**
     * Calculate HMAC-SHA256 hash for a given content and a signing key.
     *
     * @param content The content to be signed
     * @param key The key used for signing
     * @return HMAC-SHA256 BASE64 Encoded ASCII String
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static String getHmac(String content, String key) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String algorithm = "HmacSHA256";
        Mac sha256_HMAC = Mac.getInstance(algorithm);

        byte[] key_hex = new byte[0];//org.apache.commons.codec.binary.Hex.decodeHex(key.toCharArray());
        try {
            key_hex = Hex.decodeHex(key.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
        }

        SecretKeySpec secret_key = new SecretKeySpec(key_hex, algorithm);

        sha256_HMAC.init(secret_key);
        byte[] raw = sha256_HMAC.doFinal(content.getBytes("UTF-8"));

        String hmac =  new String(Hex.encodeHex(raw));//android.util.Base64.encodeToString(raw, 16);
        return hmac;
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
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(getMethod());
        connection.setRequestProperty("User-Agent", API_USER_AGENT);
        connection.setRequestProperty("Content-Type", "application/json"); //;charset=UTF-8
        connection.setRequestProperty("Accept", "application/json");
//        Log.d("connection.getHeaderField(\"Payload-HMAC\")",connection.getHeaderField("Payload-HMAC"));
        setRequestParams(connection); //allow adapter to set more stuff
        
        String body = getBody();
        
        if (body.length() > 0)
        {
            connection.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));
            
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();
        }
        
        int responseCode;
        StringBuilder response = new StringBuilder();
        
        try {
            responseCode = connection.getResponseCode();
            
            BufferedReader in = new BufferedReader(
		        new InputStreamReader(connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }
        catch (IOException e) {
            responseCode = 400;
            
            BufferedReader in = new BufferedReader(
		        new InputStreamReader(connection.getErrorStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }
        
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
     * @param connection The {@link HttpURLConnection} object that is going to execute the API call.
     */
    abstract protected void setRequestParams(HttpURLConnection connection);
    /**
     * This method let's the {@link GambitRequest} inheriting object build it's own {@link GambitResponse} object
     * @param response The RAW HTTP response body as text
     * @param code The RAW HTTP response code as an integer
     * @return An {@link GambitResponse} inheriting object
     */
    abstract protected GambitResponse getResponse(String response, int code);

}
