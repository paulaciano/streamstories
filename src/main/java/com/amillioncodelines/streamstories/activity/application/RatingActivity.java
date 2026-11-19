package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.domain.RatingRepository;
import com.amillioncodelines.streamstories.catalog.ContentActivity;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import java.util.Collection;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Rule C11: a movie or a series with ratings cannot be deleted. Episodes are never rated (rule A10). */
@Component
class RatingActivity implements ContentActivity {

    private final RatingRepository ratings;

    RatingActivity(RatingRepository ratings) {
        this.ratings = ratings;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsForAny(ContentKind kind, Collection<UUID> contentIds) {
        return kind != ContentKind.EPISODE && ratings.existsByMediaItemIdIn(contentIds);
    }
}
