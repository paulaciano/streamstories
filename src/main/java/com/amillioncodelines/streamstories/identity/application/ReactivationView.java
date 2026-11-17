package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.ReactivationRequest;
import com.amillioncodelines.streamstories.identity.domain.ReactivationStatus;
import java.time.Instant;
import java.util.UUID;

/** Read-only view of a reactivation request together with the user who made it. */
public record ReactivationView(
        UUID id,
        UUID userId,
        String email,
        String displayName,
        ReactivationStatus status,
        Instant requestedAt,
        Instant resolvedAt,
        UUID resolvedBy) {

    static ReactivationView of(ReactivationRequest request) {
        return new ReactivationView(
                request.getId(),
                request.getUser().getId(),
                request.getUser().getEmail(),
                request.getUser().getDisplayName(),
                request.getStatus(),
                request.getRequestedAt(),
                request.getResolvedAt(),
                request.getResolvedBy());
    }
}
