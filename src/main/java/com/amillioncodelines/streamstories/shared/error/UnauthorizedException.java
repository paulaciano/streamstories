package com.amillioncodelines.streamstories.shared.error;

/** Thrown when credentials or tokens are missing or invalid. Mapped to {@code 401 Unauthorized}. */
public class UnauthorizedException extends ProblemException {

    public UnauthorizedException(String message) {
        super(ProblemType.UNAUTHORIZED, message);
    }
}
