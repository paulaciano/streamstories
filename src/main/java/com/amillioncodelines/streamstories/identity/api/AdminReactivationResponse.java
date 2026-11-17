package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.application.ReactivationView;
import com.amillioncodelines.streamstories.identity.domain.ReactivationStatus;
import java.time.Instant;
import java.util.UUID;

/** What an administrator sees about a reactivation request. */
record AdminReactivationResponse(
        UUID id,
        UUID userId,
        String email,
        String displayName,
        ReactivationStatus status,
        Instant requestedAt,
        Instant resolvedAt,
        UUID resolvedBy) {

    static AdminReactivationResponse from(ReactivationView request) {
        return new AdminReactivationResponse(
                request.id(),
                request.userId(),
                request.email(),
                request.displayName(),
                request.status(),
                request.requestedAt(),
                request.resolvedAt(),
                request.resolvedBy());
    }
}
