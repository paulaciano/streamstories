package com.amillioncodelines.streamstories.shared.error;

public class ConflictException extends ProblemException {

    public ConflictException(String message) {
        super(ProblemType.CONFLICT, message);
    }
}
