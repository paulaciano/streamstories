package com.amillioncodelines.streamstories.shared.error;

import java.time.Duration;

public class TooManyRequestsException extends ProblemException {

    private final Duration retryAfter;

    public TooManyRequestsException(String message, Duration retryAfter) {
        super(ProblemType.TOO_MANY_REQUESTS, message);
        this.retryAfter = retryAfter;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}
