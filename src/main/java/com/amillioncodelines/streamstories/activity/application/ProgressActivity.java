package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.domain.WatchProgressRepository;
import com.amillioncodelines.streamstories.catalog.ContentActivity;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.util.Collection;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rule C11: a movie or an episode with watch progress cannot be deleted. A series is covered through its episodes,
 * which the catalog checks together.
 */
@Component
class ProgressActivity implements ContentActivity {

    private final WatchProgressRepository progress;

    ProgressActivity(WatchProgressRepository progress) {
        this.progress = progress;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsForAny(ContentKind kind, Collection<UUID> contentIds) {
        return switch (kind) {
            case MOVIE -> progress.existsByMovieIdIn(contentIds);
            case EPISODE -> progress.existsByEpisodeIdIn(contentIds);
            case SERIES -> false;
        };
    }
}
