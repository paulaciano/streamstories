package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.shared.error.PreconditionFailedException;
import org.springframework.http.ResponseEntity;

/**
 * An ETag is the title's version plus a hash of the response, because the response also depends on the ratings and
 * on who asks: a 304 must never hide a change. If-Match only compares the version.
 */
final class ETags {

    private ETags() {}

    static String of(long version, Object body) {
        return "\"%d-%08x\"".formatted(version, body.toString().hashCode());
    }

    static <T> ResponseEntity<T> withVersion(long version, T body) {
        return ResponseEntity.ok().eTag(of(version, body)).body(body);
    }

    static Long expectedVersion(String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank() || ifMatch.strip().equals("*")) {
            return null;
        }
        String tag = ifMatch.strip();
        // If-Match uses the strong comparison (RFC 9110): a weak ETag never matches.
        if (!tag.startsWith("\"") || !tag.endsWith("\"") || tag.length() < 3) {
            throw invalid();
        }
        String version = tag.substring(1, tag.length() - 1).split("-", 2)[0];
        try {
            return Long.parseLong(version);
        } catch (NumberFormatException ex) {
            throw invalid();
        }
    }

    private static PreconditionFailedException invalid() {
        return new PreconditionFailedException("If-Match must be an ETag returned by this API.");
    }
}
