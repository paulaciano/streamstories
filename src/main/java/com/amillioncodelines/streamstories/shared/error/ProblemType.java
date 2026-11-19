package com.amillioncodelines.streamstories.shared.error;

import java.net.URI;
import org.springframework.http.HttpStatus;

/** Machine-readable problem types returned in the {@code type} field of every error response (RFC 9457). */
public enum ProblemType {
    VALIDATION_ERROR("validation-error", "Invalid data", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST("malformed-request", "Malformed request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("unauthorized", "Unauthenticated", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", "Access denied", HttpStatus.FORBIDDEN),
    ACCOUNT_INACTIVE("account-inactive", "Inactive account", HttpStatus.FORBIDDEN),
    NOT_FOUND("not-found", "Resource not found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("method-not-allowed", "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    NOT_ACCEPTABLE("not-acceptable", "Not acceptable", HttpStatus.NOT_ACCEPTABLE),
    CONFLICT("conflict", "Conflict", HttpStatus.CONFLICT),
    CONTENT_UNAVAILABLE("content-unavailable", "Content unavailable", HttpStatus.CONFLICT),
    UNSUPPORTED_MEDIA_TYPE("unsupported-media-type", "Unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INTERNAL_ERROR("internal-error", "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);

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
