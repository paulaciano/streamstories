package com.amillioncodelines.streamstories.shared.error;

import java.net.URI;
import org.springframework.http.HttpStatus;

/** Machine-readable problem types returned in the {@code type} field of every error response (RFC 9457). */
public enum ProblemType {
    VALIDATION_ERROR("validation-error", "Datos inválidos", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST("malformed-request", "Solicitud mal formada", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("unauthorized", "No autenticado", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", "Acceso denegado", HttpStatus.FORBIDDEN),
    ACCOUNT_INACTIVE("account-inactive", "Cuenta inactiva", HttpStatus.FORBIDDEN),
    NOT_FOUND("not-found", "Recurso no encontrado", HttpStatus.NOT_FOUND),
    CONFLICT("conflict", "Conflicto", HttpStatus.CONFLICT),
    CONTENT_UNAVAILABLE("content-unavailable", "Contenido no disponible", HttpStatus.CONFLICT),
    INTERNAL_ERROR("internal-error", "Error interno", HttpStatus.INTERNAL_SERVER_ERROR);

    private static final String URN_PREFIX = "urn:streamstories:problem:";

    private final URI uri;
    private final String title;
    private final HttpStatus status;

    ProblemType(String slug, String title, HttpStatus status) {
        this.uri = URI.create(URN_PREFIX + slug);
        this.title = title;
        this.status = status;
    }

    public URI uri() {
        return uri;
    }

    public String title() {
        return title;
    }

    public HttpStatus status() {
        return status;
    }
}
