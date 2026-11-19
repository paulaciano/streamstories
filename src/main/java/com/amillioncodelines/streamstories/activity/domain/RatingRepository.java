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

public interface RatingRepository extends JpaRepository<Rating, UUID> {

    /** Creates or replaces the rating in a single statement, so simultaneous requests never fail (rule A9). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
            INSERT INTO rating (id, user_id, media_item_id, score, created_at, updated_at)
            VALUES (uuidv7(), :userId, :mediaItemId, :score, :now, :now)
            ON CONFLICT (user_id, media_item_id) DO UPDATE
            SET score = EXCLUDED.score,
                updated_at = EXCLUDED.updated_at
            """)
    void upsert(
            @Param("userId") UUID userId,
            @Param("mediaItemId") UUID mediaItemId,
            @Param("score") int score,
            @Param("now") Instant now);

    @Modifying
    @Query("delete from Rating r where r.userId = :userId and r.mediaItemId = :mediaItemId")
    void deleteByUserIdAndMediaItemId(@Param("userId") UUID userId, @Param("mediaItemId") UUID mediaItemId);

    Optional<Rating> findByUserIdAndMediaItemId(UUID userId, UUID mediaItemId);

    Page<Rating> findByUserId(UUID userId, Pageable pageable);

    boolean existsByMediaItemIdIn(Collection<UUID> mediaItemIds);

    @Query("select avg(r.score) as average, count(r) as count from Rating r where r.mediaItemId = :mediaItemId")
    RatingStats statsOf(@Param("mediaItemId") UUID mediaItemId);

    interface RatingStats {

        Double getAverage();

        long getCount();
    }

    List<Rating> findByUserId(UUID userId);

    @Query("select r.mediaItemId as mediaItemId, avg(r.score) as average, count(r) as count"
            + " from Rating r group by r.mediaItemId")
    List<TitleStats> statsByTitle();

    interface TitleStats {

        UUID getMediaItemId();

        double getAverage();

        long getCount();
    }
}
