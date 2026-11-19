package com.amillioncodelines.streamstories.activity.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WatchlistRepository extends JpaRepository<WatchlistEntry, WatchlistEntry.Key> {

    /** Adding a title that is already there keeps its original date. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(nativeQuery = true, value = """
            INSERT INTO watchlist_entry (user_id, media_item_id, added_at)
            VALUES (:userId, :mediaItemId, :now)
            ON CONFLICT (user_id, media_item_id) DO NOTHING
            """)
    void add(@Param("userId") UUID userId, @Param("mediaItemId") UUID mediaItemId, @Param("now") Instant now);

    @Modifying
    @Query("delete from WatchlistEntry e where e.key.userId = :userId and e.key.mediaItemId = :mediaItemId")
    void remove(@Param("userId") UUID userId, @Param("mediaItemId") UUID mediaItemId);

    Page<WatchlistEntry> findByKeyUserId(UUID userId, Pageable pageable);

    boolean existsByKeyMediaItemIdIn(Collection<UUID> mediaItemIds);
}
