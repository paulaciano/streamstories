package com.amillioncodelines.streamstories.activity.domain;

import com.amillioncodelines.streamstories.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The score from 1 to 5 that a user gives to a title (rules A9 and A10).
 *
 * <p>Rows are written with an atomic upsert ({@link RatingRepository}), so the entity is only read.
 */
@Entity
public class Rating extends BaseEntity {

    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private UUID mediaItemId;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = false)
    private int score;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Rating() {}

    public static void requireValidScore(int score) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException("Scores go from %d to %d".formatted(MIN_SCORE, MAX_SCORE));
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getMediaItemId() {
        return mediaItemId;
    }

    public int getScore() {
        return score;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
