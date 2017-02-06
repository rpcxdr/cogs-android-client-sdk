package io.cogswell.sdk.pubsub.exceptions;

public class RuntimeParseException extends RuntimeException {
    public RuntimeParseException() {
        super();
    }

    public RuntimeParseException(String message) {
        super(message);
    }

    public RuntimeParseException(String message, Throwable cause) {
        super(message, cause);
    }

}