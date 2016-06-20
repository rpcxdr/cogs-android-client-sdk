package io.cogswell.sdk.response;

import okhttp3.OkHttpClient;

/**
 * Created by gambit on 6/17/16.
 */
public class OkHttpClientSingleton {
    private static OkHttpClient client = new OkHttpClient();

    private OkHttpClientSingleton() {}

    public static OkHttpClient getInstance() {
        return client;
    }
}
