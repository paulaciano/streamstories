package com.amillioncodelines.streamstories.shared.error;

import java.util.Map;

/**
 * Base class for business exceptions that map directly to a {@link ProblemType}.
 *
 * <p>The message becomes the {@code detail} of the response, so it must be safe to show to API clients.
 * Optional properties are added as extension members of the problem.
 */
public abstract class ProblemException extends RuntimeException {

    private final ProblemType type;
    private final Map<String, Object> properties;

    protected ProblemException(ProblemType type, String detail) {
        this(type, detail, Map.of());
    }

    protected ProblemException(ProblemType type, String detail, Map<String, Object> properties) {
        super(detail);
        this.type = type;
        this.properties = Map.copyOf(properties);
    }

    public ProblemType type() {
        return type;
    }

    public Map<String, Object> properties() {
        return properties;
    }
}
