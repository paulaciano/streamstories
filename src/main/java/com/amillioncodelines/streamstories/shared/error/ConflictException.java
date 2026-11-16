package com.amillioncodelines.streamstories.shared.error;

/**
 * Thrown when a request conflicts with the current state of a resource (duplicates, resources in use).
 * Mapped to {@code 409 Conflict}.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
