package com.amillioncodelines.streamstories.recommendation.application;

import com.amillioncodelines.streamstories.activity.ActivityQueries;
import com.amillioncodelines.streamstories.catalog.CatalogQueries;
import com.amillioncodelines.streamstories.recommendation.domain.Recommendation;
import com.amillioncodelines.streamstories.recommendation.domain.RecommendationEngine;
import com.amillioncodelines.streamstories.shared.web.Pageables;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Computes the recommendations of a user on each request, over the whole catalog. If the catalog grows, the result can
 * be cached or precomputed later (docs/03, section 3.5).
 */
@Service
public class RecommendationService {

    private final CatalogQueries catalog;
    private final ActivityQueries activity;
    private final RecommendationEngine engine = new RecommendationEngine();

    RecommendationService(CatalogQueries catalog, ActivityQueries activity) {
        this.catalog = catalog;
        this.activity = activity;
    }

    public Page<Recommendation> recommend(UUID userId, Pageable pageable) {
        List<Recommendation> all =
                engine.recommend(catalog.findAllTitles(), activity.activityOf(userId), activity.communityRatings());
        return Pageables.slice(all, pageable);
    }
}
