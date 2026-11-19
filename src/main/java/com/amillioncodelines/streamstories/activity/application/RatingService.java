package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.domain.Rating;
import com.amillioncodelines.streamstories.activity.domain.RatingRepository;
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
public class RatingService {

    private final RatingRepository ratings;
    private final CatalogQueries catalog;
    private final Clock clock;

    RatingService(RatingRepository ratings, CatalogQueries catalog, Clock clock) {
        this.ratings = ratings;
        this.catalog = catalog;
        this.clock = clock;
    }

    @Transactional
    public RatingView rate(UUID userId, UUID titleId, int score) {
        Rating.requireValidScore(score);
        TitleSummary title = catalog.findTitle(titleId)
                .orElseThrow(() -> new ResourceNotFoundException("Title %s does not exist.".formatted(titleId)));
        if (!title.available()) {
            throw new ContentUnavailableException("Title %s is retired and cannot be rated.".formatted(titleId));
        }
        ratings.upsert(userId, titleId, score, clock.instant());
        return view(ratings.findByUserIdAndMediaItemId(userId, titleId).orElseThrow(), title);
    }

    @Transactional
    public void remove(UUID userId, UUID titleId) {
        ratings.deleteByUserIdAndMediaItemId(userId, titleId);
    }

    @Transactional(readOnly = true)
    public Page<RatingView> list(UUID userId, Pageable pageable) {
        Page<Rating> page = ratings.findByUserId(userId, pageable);
        // A title cannot be deleted while it has ratings (rule C11), so every rated title is found.
        Map<UUID, TitleSummary> titles = page.isEmpty()
                ? Map.of()
                : catalog.findTitles(page.map(Rating::getMediaItemId).getContent());
        return page.map(rating -> view(rating, titles.get(rating.getMediaItemId())));
    }

    private static RatingView view(Rating rating, TitleSummary title) {
        return new RatingView(
                title.id(),
                title.type(),
                title.title(),
                title.coverImageUrl(),
                title.available(),
                rating.getScore(),
                rating.getUpdatedAt());
    }
}
