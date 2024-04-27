package org.lsd.ccregistery.http;

import java.util.concurrent.Future;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author nhsoft.lsd
 */
public class DeferredResultWrapper<T> {
    private final long LONG_POLLING_TIMEOUT = 60_000L;
    private DeferredResult<T> deferredResult = new DeferredResult<>(LONG_POLLING_TIMEOUT);
    private Future<?> future;

    public DeferredResultWrapper() {
    }

    public void onTimeout(Runnable timeoutCallback) {
        deferredResult.onTimeout(timeoutCallback);
    }

    public void onCompletion(Runnable completionCallback) {
        deferredResult.onCompletion(completionCallback);
    }


    public void setResult(@Nullable T result) {
        deferredResult.setResult(result);
    }

    public void setErrorResult(Object result) {
        deferredResult.setErrorResult(result);
    }

    public DeferredResult<T> getDeferredResult() {
        return deferredResult;
    }

    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(final Future<?> future) {
        this.future = future;
    }
}
