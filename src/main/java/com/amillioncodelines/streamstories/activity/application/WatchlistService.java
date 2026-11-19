package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.domain.WatchlistEntry;
import com.amillioncodelines.streamstories.activity.domain.WatchlistRepository;
import com.amillioncodelines.streamstories.catalog.CatalogQueries;
import com.amillioncodelines.streamstories.catalog.TitleSummary;
import com.amillioncodelines.streamstories.shared.error.ContentUnavailableException;
import com.amillioncodelines.streamstories.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlist;
    private final CatalogQueries catalog;
    private final Clock clock;

    WatchlistService(WatchlistRepository watchlist, CatalogQueries catalog, Clock clock) {
        this.watchlist = watchlist;
        this.catalog = catalog;
        this.clock = clock;
    }

    @Transactional
    public void add(UUID userId, UUID titleId) {
        TitleSummary title = catalog.findTitle(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title %s does not exist.".formatted(titleId)));
        if (!title.available()) {
            throw new ContentUnavailableException(
                    "Title %s is retired and cannot be added to the watchlist.".formatted(titleId));
        }
        watchlist.add(userId, titleId, clock.instant());
    }

    @Transactional
    public void remove(UUID userId, UUID titleId) {
        watchlist.remove(userId, titleId);
    }

    @Transactional(readOnly = true)
    public Page<WatchlistItemView> list(UUID userId, Pageable pageable) {
        Page<WatchlistEntry> page = watchlist.findByKeyUserId(userId, pageable);
        // A title cannot be deleted while it is in a watchlist (rule C11), so every entry is found.
        Map<UUID, TitleSummary> titles = page.isEmpty()
                ? Map.of()
                : catalog.findTitles(page.map(WatchlistEntry::getMediaItemId).getContent());
        return page.map(entry -> {
            TitleSummary title = titles.get(entry.getMediaItemId());
            return new WatchlistItemView(
                    title.id(),
                    title.type(),
                    title.title(),
                    title.coverImageUrl(),
                    title.available(),
                    entry.getAddedAt());
        });
    }
}
