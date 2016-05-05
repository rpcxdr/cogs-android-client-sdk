package io.cogswell.sdk.subscription;

/**
 * Created by jedwards on 5/5/16.
 *
 * This represents a unit of work to be performed upon the completion of some operation.
 */
public interface Callback<T> {
    /**
     * The work to be performed.
     *
     * @param arg the argument to be passed to the callback.
     */
    public void call(T arg);
}
