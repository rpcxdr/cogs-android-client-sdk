package io.cogswell.sdk.pubsub;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class PubSubHandle {
    private String[] keys;
    private PubSubOptions options;
    SettableFuture<PubSubHandle> setupFuture;

    /**
     * This class can only be instantiated using it's factory method.  This allows the construction
     * to be completed asynchronously.
     */
    private PubSubHandle(){};
    public static ListenableFuture<PubSubHandle> connect(String[] keys, PubSubOptions options){
        PubSubHandle h = new PubSubHandle();
        h.keys = keys;
        h.options = options;
        h.reconnect();
        h.setupFuture = SettableFuture.create();
        h.setupFuture.set(h);
        return h.setupFuture;
    }

    private synchronized void reconnect() {
        /*
        if (!done.isMarked) {
            val sock = new PubSubSocket(keys, options)

            sock.onClose(cause => {
                if (!done.isMarked) {
                    Scheduler.schedule(reconnectDelay)(reconnect)
                }

                Try(closeHandler.foreach(_(cause))) match {
                    case Failure(error) => errorHandler.foreach(_(error, None, None))
                    case Success(_) =>
                }
            })

            socket = Some(sock)

            sock.connect() onComplete {
                case Success(_) => setupPromise.success(this)
                case Failure(error) => {
                    setupPromise.failure(error)
                    done.mark
                }
            }
        }*/
    }

}
