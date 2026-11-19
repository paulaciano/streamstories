package com.amillioncodelines.streamstories.recommendation.api;

import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.recommendation.application.RecommendationService;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import com.amillioncodelines.streamstories.shared.web.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/recommendations")
@Tag(name = "Recommendations", description = "Personalized recommendations for the current user")
class RecommendationController {

    private final RecommendationService recommendations;

    RecommendationController(RecommendationService recommendations) {
        this.recommendations = recommendations;
    }

    @GetMapping
    @Operation(
            summary = "Recommended titles, each with its reason",
            description = "Available titles the user has not rated or started, ranked by the genres they like. Without"
                    + " enough activity, the best rated titles. The order is fixed and a sort is ignored.")
    PageResponse<RecommendationResponse> recommend(
            @AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        Pageable page = Pageables.fixedOrder(pageable, Sort.unsorted());
        return PageResponse.of(recommendations.recommend(CurrentUser.id(jwt), page), RecommendationResponse::from);
    }
}
