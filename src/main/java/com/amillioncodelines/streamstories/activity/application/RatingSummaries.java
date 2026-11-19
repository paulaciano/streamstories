package com.amillioncodelines.streamstories.activity.application;

import com.amillioncodelines.streamstories.activity.domain.Rating;
import com.amillioncodelines.streamstories.activity.domain.RatingRepository;
import com.amillioncodelines.streamstories.activity.domain.RatingRepository.RatingStats;
import com.amillioncodelines.streamstories.catalog.RatingSummary;
import com.amillioncodelines.streamstories.catalog.TitleRatings;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class RatingSummaries implements TitleRatings {

    private final RatingRepository ratings;

    RatingSummaries(RatingRepository ratings) {
        this.ratings = ratings;
    }

    @Override
    @Transactional(readOnly = true)
    public RatingSummary summarize(UUID titleId, UUID viewerId) {
        RatingStats stats = ratings.statsOf(titleId);
        Integer viewerScore = ratings.findByUserIdAndMediaItemId(viewerId, titleId)
                .map(Rating::getScore)
                .orElse(null);
        return new RatingSummary(oneDecimal(stats.getAverage()), stats.getCount(), viewerScore);
    }

    static Double oneDecimal(Double average) {
        return average == null
                ? null
                : BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
