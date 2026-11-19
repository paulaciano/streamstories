package com.amillioncodelines.streamstories.identity.api;

import com.amillioncodelines.streamstories.identity.application.ReactivationView;
import com.amillioncodelines.streamstories.identity.domain.ReactivationStatus;
import java.time.Instant;
import java.util.UUID;

record ReactivationRequestResponse(UUID id, ReactivationStatus status, Instant requestedAt) {

    static ReactivationRequestResponse from(ReactivationView request) {
        return new ReactivationRequestResponse(request.id(), request.status(), request.requestedAt());
    }
}
