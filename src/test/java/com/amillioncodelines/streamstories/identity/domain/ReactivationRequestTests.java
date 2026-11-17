package com.amillioncodelines.streamstories.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amillioncodelines.streamstories.shared.error.ConflictException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReactivationRequestTests {

    private static final Instant NOW = Instant.parse("2026-11-17T10:00:00Z");
    private static final UUID ADMIN_ID = UUID.randomUUID();

    private final User user = User.register("viewer@example.com", "{noop}hash", "Viewer");

    @Test
    void shouldRecordWhoApprovedAndWhen() {
        var request = ReactivationRequest.pending(user, NOW.minusSeconds(60));

        request.approve(ADMIN_ID, NOW);

        assertThat(request.getStatus()).isEqualTo(ReactivationStatus.APPROVED);
        assertThat(request.getResolvedBy()).isEqualTo(ADMIN_ID);
        assertThat(request.getResolvedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldNotResolveTwice() {
        var request = ReactivationRequest.pending(user, NOW);
        request.reject(ADMIN_ID, NOW);

        assertThatThrownBy(() -> request.approve(ADMIN_ID, NOW)).isInstanceOf(ConflictException.class);
        assertThat(request.getStatus()).isEqualTo(ReactivationStatus.REJECTED);
    }
}
