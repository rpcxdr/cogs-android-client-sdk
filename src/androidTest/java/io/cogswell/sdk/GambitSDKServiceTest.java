package io.cogswell.sdk;

import android.util.Log;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import io.cogswell.sdk.request.GambitRequestEvent;
import io.cogswell.sdk.response.GambitResponseEvent;

public class GambitSDKServiceTest extends TestCase {
    public void testGetInstance() throws Exception {
        GambitSDKService gss = GambitSDKService.getInstance();
        assertNotNull(gss);

//guava
        //Promise<Connection> c = GambitSDKService.getInstance().newPubsubConnection(keys, opetions.);

    }
    public void testPostEvent() throws Exception {
        String timestamp = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")).format(new Date());
        String namespace = "Testing";
        String eventName = "e1";
        Integer campaignId = -1;
        JSONObject attributes = new JSONObject();
        attributes.put("email", "test@email.com");
        String accessKey = "a66ec003338b6ef20d2bab20d79e11ae";
        String clientSalt = "8121acc3e3bb630523364db444abede051bc9c7222d3800dbcb3e9156e4c9f53";
        String clientSecret = "7ca90b848a737d16fdeaf46cc9e1ec869bdf99b1c406072972b07a0c8b966da9";
        GambitRequestEvent.setBaseUrl("INVALID");
        GambitRequestEvent.Builder builder = new GambitRequestEvent.Builder(
            accessKey, clientSalt, clientSecret
        ).setEventName(eventName)
            .setNamespace(namespace)
            .setAttributes(attributes)
            .setCampaignId(campaignId)
            .setTimestamp(timestamp);

        Future<GambitResponse> future = null;
        future = GambitSDKService.getInstance().sendGambitEvent(builder);

        GambitResponseEvent response;
        response = (GambitResponseEvent) future.get();
        // This can throw InterruptedException | ExecutionException ex

        String message = response.getMessage();
        assertNotNull(message);
        assertEquals("request processed successfully", message);

//guava
        //Promise<Connection> c = GambitSDKService.getInstance().newPubsubConnection(keys, opetions.);

    }

}