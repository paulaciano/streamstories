package com.amillioncodelines.streamstories.shared.error;

public class InvalidRequestException extends ProblemException {

    public InvalidRequestException(String message) {
        super(ProblemType.VALIDATION_ERROR, message);
    }
}
