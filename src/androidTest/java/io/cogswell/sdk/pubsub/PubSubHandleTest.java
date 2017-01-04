package io.cogswell.sdk.pubsub;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PubSubHandleTest extends TestCase {
    Object result = null;

    public void testConnect() throws Exception {
        result = null;

        final CountDownLatch signal = new CountDownLatch(1);
        Object result = null;

        ListenableFuture<PubSubHandle> lf = PubSubHandle.connect(null,null);
        assertNotNull(lf);
        Futures.addCallback(lf, new FutureCallback<PubSubHandle>() {
            // we want this handler to run immediately after we push the big red button!
            public void onSuccess(PubSubHandle psh) {
                PubSubHandleTest.this.result = psh;
                signal.countDown();
            }
            public void onFailure(Throwable error) {
                PubSubHandleTest.this.result = error;
                signal.countDown();
            }
        });

        signal.await(30, TimeUnit.SECONDS);

        assertTrue(PubSubHandleTest.this.result instanceof PubSubHandle);
        //assertTrue(PubSubHandleTest.this.result instanceof Throwable);

    }

}