package com.amillioncodelines.streamstories.shared.error;

public class ContentUnavailableException extends ProblemException {

    public ContentUnavailableException(String message) {
        super(ProblemType.CONTENT_UNAVAILABLE, message);
    }
}
