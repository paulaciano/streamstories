package com.amillioncodelines.streamstories.shared.error;

import java.net.URI;

/** Machine-readable problem types returned in the {@code type} field of every error response (RFC 9457). */
public enum ProblemType {
    VALIDATION_ERROR("validation-error", "Datos inválidos"),
    MALFORMED_REQUEST("malformed-request", "Solicitud mal formada"),
    NOT_FOUND("not-found", "Recurso no encontrado"),
    CONFLICT("conflict", "Conflicto"),
    INTERNAL_ERROR("internal-error", "Error interno");

    private static final String URN_PREFIX = "urn:streamstories:problem:";

    private final URI uri;
    private final String title;

    ProblemType(String slug, String title) {
        this.uri = URI.create(URN_PREFIX + slug);
        this.title = title;
    }

    public URI uri() {
        return uri;
    }

    public String title() {
        return title;
    }
}
