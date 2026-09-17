package com.govscheme.scheme.client;

/**
 * Thrown for myScheme API failures. Retryable = 429 / 5xx / transport errors.
 * Other 4xx responses are permanent for the attempted call.
 */
public class MySchemeException extends RuntimeException {

    private final int statusCode;
    private final boolean retryable;

    public MySchemeException(String message, int statusCode, boolean retryable) {
        super(message);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    public MySchemeException(String message, int statusCode, boolean retryable, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    public int getStatusCode() { return statusCode; }

    public boolean isRetryable() { return retryable; }
}
