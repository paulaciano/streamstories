package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.MovieService;
import com.amillioncodelines.streamstories.catalog.application.TitleRatingService;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import com.amillioncodelines.streamstories.catalog.domain.CatalogFilter;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies")
class MovieController {

    private final MovieService movies;
    private final TitleRatingService ratings;

    MovieController(MovieService movies, TitleRatingService ratings) {
        this.movies = movies;
        this.ratings = ratings;
    }

    @GetMapping
    @Operation(summary = "Lists movies, filterable by genre and text")
    PageResponse<MovieResponse> list(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String q,
            @Parameter(description = "ADMIN only") @RequestParam(required = false) Availability availability,
            @ParameterObject Pageable pageable,
            Authentication authentication) {
        var filter = new CatalogFilter(genre, null, q, availability);
        var page = movies.list(filter, Audiences.of(authentication), TitleSorting.restrict(pageable));
        return PageResponse.of(page, MovieResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Movie detail, with its ratings",
            description = "A retired movie is still reachable by id, without a video URL for a USER.")
    MovieResponse get(@PathVariable UUID id, Authentication authentication) {
        return MovieResponse.from(
                movies.get(id, Audiences.of(authentication)),
                ratings.summarize(id, Audiences.viewerId(authentication)));
    }

    @PostMapping
    @Operation(summary = "Creates a movie (ADMIN)")
    ResponseEntity<MovieResponse> create(@Valid @RequestBody MovieRequest request) {
        MovieResponse movie = MovieResponse.from(movies.create(request.toCommand()));
        return ResponseEntity.created(URI.create("/api/v1/movies/" + movie.id()))
                .body(movie);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replaces a movie's data, including its genres (ADMIN)")
    MovieResponse replace(@PathVariable UUID id, @Valid @RequestBody MovieRequest request) {
        return MovieResponse.from(movies.replace(id, request.toCommand()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Retires a movie or makes it available again (ADMIN)")
    MovieResponse changeAvailability(@PathVariable UUID id, @Valid @RequestBody AvailabilityRequest request) {
        return MovieResponse.from(movies.changeAvailability(id, request.availability()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletes a movie without user activity (ADMIN)")
    void delete(@PathVariable UUID id) {
        movies.delete(id);
    }
}
