package com.amillioncodelines.streamstories.shared.error;

/**
 * Thrown when a conditional request ({@code If-Match}) refers to a version that is no longer current. Mapped to
 * {@code 412 Precondition Failed}.
 */
public class PreconditionFailedException extends ProblemException {

    public PreconditionFailedException(String message) {
        super(ProblemType.PRECONDITION_FAILED, message);
    }
}
