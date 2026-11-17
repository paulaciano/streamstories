package com.amillioncodelines.streamstories.shared.error;

/** Thrown when a requested resource does not exist. Mapped to {@code 404 Not Found}. */
public class ResourceNotFoundException extends ProblemException {

    public ResourceNotFoundException(String message) {
        super(ProblemType.NOT_FOUND, message);
    }
}
