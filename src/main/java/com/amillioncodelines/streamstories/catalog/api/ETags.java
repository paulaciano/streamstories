package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.shared.error.PreconditionFailedException;
import org.springframework.http.ResponseEntity;

/**
 * The version of a title as an HTTP entity tag. A client sends it back in {@code If-Match} to update the title only if
 * nobody changed it in the meantime; without {@code If-Match}, the update is not conditional.
 */
final class ETags {

    private ETags() {}

    static <T> ResponseEntity<T> withVersion(long version, T body) {
        return ResponseEntity.ok().eTag(String.valueOf(version)).body(body);
    }

    /**
     * @return the version expected by the client, or {@code null} for an unconditional request
     * @throws PreconditionFailedException if the header is not a version of this API
     */
    static Long expectedVersion(String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank() || ifMatch.strip().equals("*")) {
            return null;
        }
        String tag = ifMatch.strip();
        if (tag.startsWith("W/")) {
            tag = tag.substring(2);
        }
        tag = tag.replace("\"", "");
        try {
            return Long.parseLong(tag);
        } catch (NumberFormatException ex) {
            throw new PreconditionFailedException("If-Match must be an ETag returned by this API.");
        }
    }
}
