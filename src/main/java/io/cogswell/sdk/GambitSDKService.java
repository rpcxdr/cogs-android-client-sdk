package io.cogswell.sdk;

import io.cogswell.sdk.request.GambitRequestEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class GambitSDKService {

    /**
     * Singleton instance
     */
    protected static GambitSDKService mInstance;

    /**
     * Thread loop
     */
    protected final ExecutorService mExecutor;

    /**
     * Singleton constructor
     */
    protected GambitSDKService() throws RuntimeException {
        mExecutor = Executors.newCachedThreadPool();

    }

    /**
     * Creates a {@link GambitSDKService} if none previously existed in the VM,
     * otherwise returns the existing {@link GambitSDKService} instance.
     * @return GambitSDKService
     */
    public static GambitSDKService getInstance() throws RuntimeException {
        if (mInstance == null) {
            mInstance = new GambitSDKService();
        }

        return mInstance;
    }
    /**
     * Get the main thread pool
     * @return executor service
     */
    protected ExecutorService getExecutorService() {
        return mExecutor;
    }

    /**
     * Send Gambit Event data
     * @param builder Builder that configures the {@link GambitRequest} inheriting object
     * @return Promised object that inherits {@link GambitResponse}
     * @throws java.lang.Exception
     */
    public Future<GambitResponse> sendGambitEvent(GambitRequestEvent.Builder builder) throws Exception {
        return mExecutor.submit(builder.build());
    }


}
