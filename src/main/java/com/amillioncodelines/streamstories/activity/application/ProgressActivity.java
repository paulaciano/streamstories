package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.domain.WatchProgressRepository;
import com.amillioncodelines.streamstories.catalog.ContentActivity;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rule C11: a movie or an episode with watch progress cannot be deleted. A series is covered through its episodes,
 * which the catalog checks one by one.
 */
@Component
class ProgressActivity implements ContentActivity {

    private final WatchProgressRepository progress;

    ProgressActivity(WatchProgressRepository progress) {
        this.progress = progress;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(ContentKind kind, UUID contentId) {
        return switch (kind) {
            case MOVIE -> progress.existsByMovieId(contentId);
            case EPISODE -> progress.existsByEpisodeId(contentId);
            case SERIES -> false;
        };
    }
}
