package com.amillioncodelines.streamstories.activity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import java.time.Instant;
import java.util.UUID;

/** Written only through the idempotent insert of {@link WatchlistRepository}. */
@Entity
public class WatchlistEntry {

    @EmbeddedId
    private Key key;

    @Column(nullable = false, updatable = false)
    private Instant addedAt;

    protected WatchlistEntry() {}

    public UUID getUserId() {
        return key.userId();
    }

    public UUID getMediaItemId() {
        return key.mediaItemId();
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    @Embeddable
    public record Key(UUID userId, UUID mediaItemId) {}
}
