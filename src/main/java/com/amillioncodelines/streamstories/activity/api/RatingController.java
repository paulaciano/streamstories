package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.RatingService;
import com.amillioncodelines.streamstories.identity.CurrentUser;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
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
@Tag(name = "Puntuaciones", description = "Puntuaciones de 1 a 5 del usuario actual sobre películas y series")
class RatingController {

    /** The order is fixed and a {@code sort} is ignored. */
    private static final Sort MOST_RECENT_FIRST = Sort.by(Sort.Direction.DESC, "updatedAt", "id");

    private final RatingService ratings;

    RatingController(RatingService ratings) {
        this.ratings = ratings;
    }

    @PutMapping("/{titleId}")
    @Operation(
            summary = "Puntúa una película o una serie",
            description = "Crea la puntuación o reemplaza la anterior. No hace falta haber visto el título."
                    + " Responde 409 si el título está retirado.")
    RatingResponse rate(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID titleId, @Valid @RequestBody RatingRequest request) {
        return RatingResponse.from(ratings.rate(CurrentUser.id(jwt), titleId, request.score()));
    }

    @DeleteMapping("/{titleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Quita la puntuación de un título",
            description = "Se puede hacer aunque el título esté retirado. Si no había puntuación, no cambia nada.")
    void remove(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID titleId) {
        ratings.remove(CurrentUser.id(jwt), titleId);
    }

    @GetMapping
    @Operation(
            summary = "Puntuaciones del usuario actual",
            description = "De la más reciente a la más antigua. Los títulos retirados aparecen con available en false.")
    PageResponse<RatingResponse> list(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        Pageable mostRecentFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), MOST_RECENT_FIRST);
        return PageResponse.of(ratings.list(CurrentUser.id(jwt), mostRecentFirst), RatingResponse::from);
    }
}
