package io.cogswell.sdk.notifications;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class GcmService extends GcmListenerService {

    public GcmService() {

    }
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        sendNotification("Received GCM Message: " + data.toString());
    }

    /**
     * Called when message is deleted.
     */
    @Override
    public void onDeletedMessages() {
        sendNotification("Deleted messages on server");
    }

    /**
     * Called when message is sent.
     */
    @Override
    public void onMessageSent(String msgId) {
        sendNotification("Upstream message sent. Id=" + msgId);
    }

    /**
     * Called when there is an error.
     */
    @Override
    public void onSendError(String msgId, String error) {
        sendNotification("Upstream message send error. Id=" + msgId + ", error" + error);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        //Log.d("msg", msg);
    }
}
