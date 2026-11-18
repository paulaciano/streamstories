package com.amillioncodelines.streamstories.activity.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WatchProgressRepository extends JpaRepository<WatchProgress, UUID> {

    /**
     * Creates or updates the progress in a single statement, so simultaneous reports for the same content never fail
     * on the unique index. A completed record stays completed (rule A3).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
            INSERT INTO watch_progress (id, user_id, movie_id, position_seconds, completed, last_watched_at)
            VALUES (uuidv7(), :userId, :movieId, :positionSeconds, :completed, :now)
            ON CONFLICT (user_id, movie_id) WHERE movie_id IS NOT NULL DO UPDATE
            SET position_seconds = EXCLUDED.position_seconds,
                completed = watch_progress.completed OR EXCLUDED.completed,
                last_watched_at = EXCLUDED.last_watched_at
            """)
    void upsertMovieProgress(
            @Param("userId") UUID userId,
            @Param("movieId") UUID movieId,
            @Param("positionSeconds") int positionSeconds,
            @Param("completed") boolean completed,
            @Param("now") Instant now);

    /** Same as {@link #upsertMovieProgress}, for an episode. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
            INSERT INTO watch_progress (id, user_id, episode_id, position_seconds, completed, last_watched_at)
            VALUES (uuidv7(), :userId, :episodeId, :positionSeconds, :completed, :now)
            ON CONFLICT (user_id, episode_id) WHERE episode_id IS NOT NULL DO UPDATE
            SET position_seconds = EXCLUDED.position_seconds,
                completed = watch_progress.completed OR EXCLUDED.completed,
                last_watched_at = EXCLUDED.last_watched_at
            """)
    void upsertEpisodeProgress(
            @Param("userId") UUID userId,
            @Param("episodeId") UUID episodeId,
            @Param("positionSeconds") int positionSeconds,
            @Param("completed") boolean completed,
            @Param("now") Instant now);

    Optional<WatchProgress> findByUserIdAndMovieId(UUID userId, UUID movieId);

    Optional<WatchProgress> findByUserIdAndEpisodeId(UUID userId, UUID episodeId);

    boolean existsByMovieId(UUID movieId);

    boolean existsByEpisodeId(UUID episodeId);
}
