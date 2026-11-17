package com.amillioncodelines.streamstories.shared.error;

/**
 * Thrown when a request is well formed but carries an invalid value that Bean Validation cannot express, such as an
 * unsupported sort field. Mapped to {@code 400 Bad Request}.
 */
public class InvalidRequestException extends ProblemException {

    public InvalidRequestException(String message) {
        super(ProblemType.VALIDATION_ERROR, message);
    }
}
