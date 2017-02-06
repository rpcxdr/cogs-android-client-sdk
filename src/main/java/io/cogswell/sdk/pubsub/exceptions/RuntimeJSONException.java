package io.cogswell.sdk.pubsub.exceptions;

import org.json.JSONException;

public class RuntimeJSONException extends RuntimeException {
    public RuntimeJSONException(String message) {
        super(message);
    }

    public RuntimeJSONException(JSONException cause) {
        super(cause);
    }

    public RuntimeJSONException(String message, JSONException cause) {
        super(message, cause);
    }
}