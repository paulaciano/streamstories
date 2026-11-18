package com.amillioncodelines.streamstories.shared.error;

/** Thrown when a request tries to play or rate content that has been retired. Mapped to {@code 409 Conflict}. */
public class ContentUnavailableException extends ProblemException {

    public ContentUnavailableException(String message) {
        super(ProblemType.CONTENT_UNAVAILABLE, message);
    }
}
