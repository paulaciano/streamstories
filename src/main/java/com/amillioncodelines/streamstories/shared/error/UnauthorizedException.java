package com.amillioncodelines.streamstories.shared.error;

public class UnauthorizedException extends ProblemException {

    public UnauthorizedException(String message) {
        super(ProblemType.UNAUTHORIZED, message);
    }
}
