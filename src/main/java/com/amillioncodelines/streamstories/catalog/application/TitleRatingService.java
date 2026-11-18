package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.RatingSummary;
import com.amillioncodelines.streamstories.catalog.TitleRatings;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/** The ratings shown in the detail of a title, from the module that records them (if any). */
@Service
public class TitleRatingService {

    private final ObjectProvider<TitleRatings> ratings;

    TitleRatingService(ObjectProvider<TitleRatings> ratings) {
        this.ratings = ratings;
    }

    public RatingSummary summarize(UUID titleId, UUID viewerId) {
        TitleRatings provider = ratings.getIfAvailable();
        return provider == null ? RatingSummary.NONE : provider.summarize(titleId, viewerId);
    }
}
