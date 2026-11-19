package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.domain.WatchlistRepository;
import com.amillioncodelines.streamstories.catalog.ContentActivity;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.util.Collection;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class WatchlistActivity implements ContentActivity {

    private final WatchlistRepository watchlist;

    WatchlistActivity(WatchlistRepository watchlist) {
        this.watchlist = watchlist;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsForAny(ContentKind kind, Collection<UUID> contentIds) {
        return kind != ContentKind.EPISODE && watchlist.existsByKeyMediaItemIdIn(contentIds);
    }
}
