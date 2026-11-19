package com.amillioncodelines.streamstories.activity.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
            INSERT INTO watch_progress (id, user_id, episode_id, series_id, position_seconds, completed, last_watched_at)
            VALUES (uuidv7(), :userId, :episodeId, :seriesId, :positionSeconds, :completed, :now)
            ON CONFLICT (user_id, episode_id) WHERE episode_id IS NOT NULL DO UPDATE
            SET position_seconds = EXCLUDED.position_seconds,
                completed = watch_progress.completed OR EXCLUDED.completed,
                last_watched_at = EXCLUDED.last_watched_at
            """)
    void upsertEpisodeProgress(
            @Param("userId") UUID userId,
            @Param("episodeId") UUID episodeId,
            @Param("seriesId") UUID seriesId,
            @Param("positionSeconds") int positionSeconds,
            @Param("completed") boolean completed,
            @Param("now") Instant now);

    Optional<WatchProgress> findByUserIdAndMovieId(UUID userId, UUID movieId);

    Optional<WatchProgress> findByUserIdAndEpisodeId(UUID userId, UUID episodeId);

    boolean existsByMovieIdIn(Collection<UUID> movieIds);

    boolean existsByEpisodeIdIn(Collection<UUID> episodeIds);

    Page<WatchProgress> findByUserId(UUID userId, Pageable pageable);

    /** Completed episodes are kept: a series can still continue after them. */
    @Query(nativeQuery = true, value = """
            SELECT * FROM (
                SELECT DISTINCT ON (series_id) *
                FROM watch_progress
                WHERE user_id = :userId AND series_id IS NOT NULL
                ORDER BY series_id, last_watched_at DESC, id DESC
            ) latest_episode
            UNION ALL
            SELECT *
            FROM watch_progress
            WHERE user_id = :userId AND movie_id IS NOT NULL AND completed = false
            ORDER BY last_watched_at DESC, id DESC
            """)
    List<WatchProgress> findContinueWatchingCandidates(@Param("userId") UUID userId);

    @Query(nativeQuery = true, value = """
            SELECT COALESCE(movie_id, series_id) AS titleId, bool_or(completed) AS completed
            FROM watch_progress
            WHERE user_id = :userId
            GROUP BY COALESCE(movie_id, series_id)
            """)
    List<StartedTitle> findStartedTitles(@Param("userId") UUID userId);

    interface StartedTitle {

        UUID getTitleId();

        boolean getCompleted();
    }
}
