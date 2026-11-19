package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeriesView;
import com.amillioncodelines.streamstories.catalog.application.SeriesService;
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
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/series")
@Tag(name = "Series", description = "Series, seasons and episodes")
class SeriesController {

    private static final String IF_MATCH =
            "Send the ETag of the detail in If-Match to change it only if nobody changed it"
                    + " since you read it: otherwise the answer is 412. Without If-Match, the change is not conditional.";

    private final SeriesService series;
    private final TitleRatingService ratings;

    SeriesController(SeriesService series, TitleRatingService ratings) {
        this.series = series;
        this.ratings = ratings;
    }

    @GetMapping
    @Operation(summary = "Lists series, filterable by genre and text")
    PageResponse<SeriesSummaryResponse> list(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String q,
            @Parameter(description = "ADMIN only") @RequestParam(required = false) Availability availability,
            @ParameterObject Pageable pageable,
            Authentication authentication) {
        var filter = new CatalogFilter(genre, null, q, availability);
        var page = series.list(filter, Audiences.of(authentication), TitleSorting.restrict(pageable));
        return PageResponse.of(page, SeriesSummaryResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Series detail, with its seasons and ratings")
    ResponseEntity<SeriesResponse> get(@PathVariable UUID id, Authentication authentication) {
        SeriesView view = series.get(id, Audiences.of(authentication));
        return ETags.withVersion(
                view.version(), SeriesResponse.from(view, ratings.summarize(id, Audiences.viewerId(authentication))));
    }

    @PostMapping
    @Operation(summary = "Creates a series (ADMIN)")
    ResponseEntity<SeriesResponse> create(@Valid @RequestBody SeriesRequest request) {
        SeriesView created = series.create(request.toCommand());
        SeriesResponse body = SeriesResponse.from(created);
        return ResponseEntity.created(URI.create("/api/v1/series/" + created.id()))
                .eTag(ETags.of(created.version(), body))
                .body(body);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replaces a series' data, including its genres (ADMIN)", description = IF_MATCH)
    ResponseEntity<SeriesResponse> replace(
            @PathVariable UUID id,
            @RequestHeader(name = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @Valid @RequestBody SeriesRequest request) {
        SeriesView view = series.replace(id, request.toCommand(), ETags.expectedVersion(ifMatch));
        return ETags.withVersion(view.version(), SeriesResponse.from(view));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Retires a whole series or makes it available again (ADMIN)", description = IF_MATCH)
    ResponseEntity<SeriesResponse> changeAvailability(
            @PathVariable UUID id,
            @RequestHeader(name = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @Valid @RequestBody AvailabilityRequest request) {
        SeriesView view = series.changeAvailability(id, request.availability(), ETags.expectedVersion(ifMatch));
        return ETags.withVersion(view.version(), SeriesResponse.from(view));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletes a series without activity, with its seasons and episodes (ADMIN)")
    void delete(@PathVariable UUID id) {
        series.delete(id);
    }

    @GetMapping("/{id}/seasons/{number}")
    @Operation(summary = "A season with its episodes")
    SeasonResponse getSeason(@PathVariable UUID id, @PathVariable int number) {
        return SeasonResponse.from(series.getSeason(id, number));
    }

    @PostMapping("/{id}/seasons")
    @Operation(summary = "Adds a season (ADMIN)")
    ResponseEntity<SeasonResponse> addSeason(@PathVariable UUID id, @Valid @RequestBody SeasonRequest request) {
        SeasonResponse season = SeasonResponse.from(series.addSeason(id, request.number(), request.title()));
        return ResponseEntity.created(URI.create("/api/v1/series/%s/seasons/%d".formatted(id, season.number())))
                .body(season);
    }

    @PutMapping("/{id}/seasons/{number}")
    @Operation(summary = "Changes a season's title (ADMIN)")
    SeasonResponse updateSeason(
            @PathVariable UUID id, @PathVariable int number, @Valid @RequestBody SeasonUpdateRequest request) {
        return SeasonResponse.from(series.updateSeason(id, number, request.title()));
    }

    @PatchMapping("/{id}/seasons/{number}")
    @Operation(summary = "Retires a whole season or makes it available again (ADMIN)")
    SeasonResponse changeSeasonAvailability(
            @PathVariable UUID id, @PathVariable int number, @Valid @RequestBody AvailabilityRequest request) {
        return SeasonResponse.from(series.changeSeasonAvailability(id, number, request.availability()));
    }

    @DeleteMapping("/{id}/seasons/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletes a season without episodes (ADMIN)")
    void deleteSeason(@PathVariable UUID id, @PathVariable int number) {
        series.deleteSeason(id, number);
    }

    @PostMapping("/{id}/seasons/{number}/episodes")
    @Operation(summary = "Adds an episode to a season (ADMIN)")
    ResponseEntity<EpisodeResponse> addEpisode(
            @PathVariable UUID id, @PathVariable int number, @Valid @RequestBody EpisodeRequest request) {
        EpisodeResponse episode = EpisodeResponse.from(series.addEpisode(id, number, request.toCommand()));
        return ResponseEntity.created(URI.create("/api/v1/episodes/" + episode.id()))
                .body(episode);
    }
}
