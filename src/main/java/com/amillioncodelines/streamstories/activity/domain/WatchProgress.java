package com.amillioncodelines.streamstories.activity.domain;

import com.amillioncodelines.streamstories.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.Instant;
import java.util.UUID;

/** Written only through the atomic upsert of {@link WatchProgressRepository}. */
@Entity
public class WatchProgress extends BaseEntity {

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Column(updatable = false)
    private UUID movieId;

    @Column(updatable = false)
    private UUID episodeId;

    /** The series of the episode, so "continue watching" can group episodes without the catalog. */
    @Column(updatable = false)
    private UUID seriesId;

    @Column(nullable = false)
    private int positionSeconds;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private Instant lastWatchedAt;

    protected WatchProgress() {}

    public UUID getUserId() {
        return userId;
    }

    public UUID getMovieId() {
        return movieId;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public UUID getSeriesId() {
        return seriesId;
    }

    public int getPositionSeconds() {
        return positionSeconds;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Instant getLastWatchedAt() {
        return lastWatchedAt;
    }
}
