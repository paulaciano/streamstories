package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.RatingService;
import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import com.amillioncodelines.streamstories.shared.web.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/ratings")
@Tag(name = "Ratings", description = "Scores from 1 to 5 that the current user gives to movies and series")
class RatingController {

    private static final Sort MOST_RECENT_FIRST = Sort.by(Sort.Direction.DESC, "updatedAt");

    private final RatingService ratings;

    RatingController(RatingService ratings) {
        this.ratings = ratings;
    }

    @PutMapping("/{titleId}")
    @Operation(
            summary = "Rates a movie or a series",
            description = "Creates the rating or replaces the previous one. The title does not need to be"
                    + " watched first. Answers 409 if the title is retired.")
    RatingResponse rate(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID titleId, @Valid @RequestBody RatingRequest request) {
        return RatingResponse.from(ratings.rate(CurrentUser.id(jwt), titleId, request.score()));
    }

    @DeleteMapping("/{titleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Removes the rating of a title",
            description = "Allowed even if the title is retired. Without a rating, nothing changes.")
    void remove(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID titleId) {
        ratings.remove(CurrentUser.id(jwt), titleId);
    }

    @GetMapping
    @Operation(
            summary = "Ratings of the current user",
            description = "Most recent first. Retired titles have available set to false.")
    PageResponse<RatingResponse> list(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        Pageable mostRecentFirst = Pageables.fixedOrder(pageable, MOST_RECENT_FIRST);
        return PageResponse.of(ratings.list(CurrentUser.id(jwt), mostRecentFirst), RatingResponse::from);
    }
}
