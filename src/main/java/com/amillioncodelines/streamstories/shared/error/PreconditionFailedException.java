package com.amillioncodelines.streamstories.shared.error;

public class PreconditionFailedException extends ProblemException {

    public PreconditionFailedException(String message) {
        super(ProblemType.PRECONDITION_FAILED, message);
    }
}
