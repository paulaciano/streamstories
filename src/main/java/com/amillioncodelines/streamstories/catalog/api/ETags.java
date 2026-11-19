package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.shared.error.PreconditionFailedException;
import org.springframework.http.ResponseEntity;

final class ETags {

    private ETags() {}

    static <T> ResponseEntity<T> withVersion(long version, T body) {
        return ResponseEntity.ok().eTag(String.valueOf(version)).body(body);
    }

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
