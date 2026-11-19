package com.amillioncodelines.streamstories.recommendation.api;

import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.recommendation.domain.Recommendation;
import java.util.UUID;

record RecommendationResponse(UUID titleId, ContentKind type, String title, String coverImageUrl, String reason) {

    static RecommendationResponse from(Recommendation recommendation) {
        var title = recommendation.title();
        return new RecommendationResponse(
                title.id(), title.type(), title.title(), title.coverImageUrl(), recommendation.reason());
    }
}
