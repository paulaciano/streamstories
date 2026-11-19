package com.amillioncodelines.streamstories.shared.error;

public class ResourceNotFoundException extends ProblemException {

    public ResourceNotFoundException(String message) {
        super(ProblemType.NOT_FOUND, message);
    }
}
