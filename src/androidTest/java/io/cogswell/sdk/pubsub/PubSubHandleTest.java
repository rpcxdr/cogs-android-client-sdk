package io.cogswell.sdk.pubsub;

import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PubSubHandleTest extends TestCase {
    Object result = null;

    String[] keys = {
            "A-*-*",
            "R-*-*",
            "W-*-*"
    };

    public void testConnect() throws Exception {
        result = null;

        final CountDownLatch signal = new CountDownLatch(1);
        Object result = null;


        ListenableFuture<PubSubHandle> lf = PubSubHandle.connect(keys, new PubSubOptions("https://gamqa-api.aviatainc.com/pubsub"));

        assertNotNull(lf);
        Futures.addCallback(lf, new FutureCallback<PubSubHandle>() {
            public void onSuccess(PubSubHandle psh) {
                PubSubHandleTest.this.result = psh;
                signal.countDown();
            }
            public void onFailure(Throwable error) {
                Log.e("TEST","Error:",error);
                PubSubHandleTest.this.result = error;
                signal.countDown();
            }
        });

        signal.await(30, TimeUnit.SECONDS);

        assertTrue(PubSubHandleTest.this.result instanceof PubSubHandle);
        //assertTrue(PubSubHandleTest.this.result instanceof Throwable);

    }

    public void testPublish() throws Exception {
        result = null;

        final CountDownLatch signal = new CountDownLatch(1);
        Object result = null;


        ListenableFuture<PubSubHandle> lf = PubSubHandle.connect(keys, new PubSubOptions("https://gamqa-api.aviatainc.com/pubsub"));

        assertNotNull(lf);
        Futures.addCallback(lf, new FutureCallback<PubSubHandle>() {
            public void onSuccess(PubSubHandle psh) {
                PubSubHandleTest.this.result = psh;
                signal.countDown();
            }
            public void onFailure(Throwable error) {
                Log.e("TEST","Error:",error);
                PubSubHandleTest.this.result = error;
                signal.countDown();
            }
        });

        signal.await(30, TimeUnit.SECONDS);

        assertTrue(PubSubHandleTest.this.result instanceof PubSubHandle);
        //assertTrue(PubSubHandleTest.this.result instanceof Throwable);

    }



}