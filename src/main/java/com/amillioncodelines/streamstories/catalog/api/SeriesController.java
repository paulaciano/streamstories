package com.amillioncodelines.streamstories.catalog.api;

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
@RequestMapping("/api/v1/series")
@Tag(name = "Series", description = "Series, temporadas y alta de episodios")
class SeriesController {

    private final SeriesService series;
    private final TitleRatingService ratings;

    SeriesController(SeriesService series, TitleRatingService ratings) {
        this.series = series;
        this.ratings = ratings;
    }

    @GetMapping
    @Operation(summary = "Lista series, filtrables por género y texto")
    PageResponse<SeriesSummaryResponse> list(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String q,
            @Parameter(description = "Solo para ADMIN") @RequestParam(required = false) Availability availability,
            @ParameterObject Pageable pageable,
            Authentication authentication) {
        var filter = new CatalogFilter(genre, null, q, availability);
        var page = series.list(filter, Audiences.of(authentication), TitleSorting.restrict(pageable));
        return PageResponse.of(page, SeriesSummaryResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una serie con el resumen de sus temporadas")
    SeriesResponse get(@PathVariable UUID id, Authentication authentication) {
        return SeriesResponse.from(
                series.get(id, Audiences.of(authentication)),
                ratings.summarize(id, Audiences.viewerId(authentication)));
    }

    @PostMapping
    @Operation(summary = "Crea una serie (ADMIN)")
    ResponseEntity<SeriesResponse> create(@Valid @RequestBody SeriesRequest request) {
        SeriesResponse created = SeriesResponse.from(series.create(request.toCommand()));
        return ResponseEntity.created(URI.create("/api/v1/series/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplaza los datos de una serie, incluidos sus géneros (ADMIN)")
    SeriesResponse replace(@PathVariable UUID id, @Valid @RequestBody SeriesRequest request) {
        return SeriesResponse.from(series.replace(id, request.toCommand()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Retira una serie completa o la vuelve a ofrecer (ADMIN)")
    SeriesResponse changeAvailability(@PathVariable UUID id, @Valid @RequestBody AvailabilityRequest request) {
        return SeriesResponse.from(series.changeAvailability(id, request.availability()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra una serie sin actividad, con sus temporadas y episodios (ADMIN)")
    void delete(@PathVariable UUID id) {
        series.delete(id);
    }

    @GetMapping("/{id}/seasons/{number}")
    @Operation(summary = "Una temporada con sus episodios")
    SeasonResponse getSeason(@PathVariable UUID id, @PathVariable int number) {
        return SeasonResponse.from(series.getSeason(id, number));
    }

    @PostMapping("/{id}/seasons")
    @Operation(summary = "Agrega una temporada (ADMIN)")
    ResponseEntity<SeasonResponse> addSeason(@PathVariable UUID id, @Valid @RequestBody SeasonRequest request) {
        SeasonResponse season = SeasonResponse.from(series.addSeason(id, request.number(), request.title()));
        return ResponseEntity.created(URI.create("/api/v1/series/%s/seasons/%d".formatted(id, season.number())))
                .body(season);
    }

    @PutMapping("/{id}/seasons/{number}")
    @Operation(summary = "Modifica el título de una temporada (ADMIN)")
    SeasonResponse updateSeason(
            @PathVariable UUID id, @PathVariable int number, @Valid @RequestBody SeasonUpdateRequest request) {
        return SeasonResponse.from(series.updateSeason(id, number, request.title()));
    }

    @PatchMapping("/{id}/seasons/{number}")
    @Operation(summary = "Retira una temporada completa o la vuelve a ofrecer (ADMIN)")
    SeasonResponse changeSeasonAvailability(
            @PathVariable UUID id, @PathVariable int number, @Valid @RequestBody AvailabilityRequest request) {
        return SeasonResponse.from(series.changeSeasonAvailability(id, number, request.availability()));
    }

    @DeleteMapping("/{id}/seasons/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra una temporada sin episodios (ADMIN)")
    void deleteSeason(@PathVariable UUID id, @PathVariable int number) {
        series.deleteSeason(id, number);
    }

    @PostMapping("/{id}/seasons/{number}/episodes")
    @Operation(summary = "Agrega un episodio a una temporada (ADMIN)")
    ResponseEntity<EpisodeResponse> addEpisode(
            @PathVariable UUID id, @PathVariable int number, @Valid @RequestBody EpisodeRequest request) {
        EpisodeResponse episode = EpisodeResponse.from(series.addEpisode(id, number, request.toCommand()));
        return ResponseEntity.created(URI.create("/api/v1/episodes/" + episode.id()))
                .body(episode);
    }
}
