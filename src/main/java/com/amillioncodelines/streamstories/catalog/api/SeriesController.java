package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.AvailabilityCommand;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.EpisodeView;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeasonView;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeriesSummaryView;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeriesView;
import com.amillioncodelines.streamstories.catalog.application.EpisodeCommand;
import com.amillioncodelines.streamstories.catalog.application.SeasonCommand;
import com.amillioncodelines.streamstories.catalog.application.SeasonUpdateCommand;
import com.amillioncodelines.streamstories.catalog.application.SeriesCommand;
import com.amillioncodelines.streamstories.catalog.application.SeriesService;
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

    SeriesController(SeriesService series) {
        this.series = series;
    }

    @GetMapping
    @Operation(summary = "Lista series, filtrables por género y texto")
    PageResponse<SeriesSummaryView> list(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String q,
            @Parameter(description = "Solo para ADMIN") @RequestParam(required = false) Availability availability,
            @ParameterObject Pageable pageable,
            Authentication authentication) {
        var filter = new CatalogFilter(genre, null, q, availability);
        var page = series.list(filter, Audiences.of(authentication), TitleSorting.restrict(pageable));
        return PageResponse.of(page, item -> item);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una serie con el resumen de sus temporadas")
    SeriesView get(@PathVariable UUID id, Authentication authentication) {
        return series.get(id, Audiences.of(authentication));
    }

    @PostMapping
    @Operation(summary = "Crea una serie (ADMIN)")
    ResponseEntity<SeriesView> create(@Valid @RequestBody SeriesCommand command) {
        SeriesView created = series.create(command);
        return ResponseEntity.created(URI.create("/api/v1/series/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplaza los datos de una serie, incluidos sus géneros (ADMIN)")
    SeriesView replace(@PathVariable UUID id, @Valid @RequestBody SeriesCommand command) {
        return series.replace(id, command);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Retira una serie completa o la vuelve a ofrecer (ADMIN)")
    SeriesView changeAvailability(@PathVariable UUID id, @Valid @RequestBody AvailabilityCommand command) {
        return series.changeAvailability(id, command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra una serie sin actividad, con sus temporadas y episodios (ADMIN)")
    void delete(@PathVariable UUID id) {
        series.delete(id);
    }

    @GetMapping("/{id}/seasons/{number}")
    @Operation(summary = "Una temporada con sus episodios")
    SeasonView getSeason(@PathVariable UUID id, @PathVariable int number, Authentication authentication) {
        return series.getSeason(id, number, Audiences.of(authentication));
    }

    @PostMapping("/{id}/seasons")
    @Operation(summary = "Agrega una temporada (ADMIN)")
    ResponseEntity<SeasonView> addSeason(@PathVariable UUID id, @Valid @RequestBody SeasonCommand command) {
        SeasonView season = series.addSeason(id, command);
        return ResponseEntity.created(URI.create("/api/v1/series/%s/seasons/%d".formatted(id, season.number())))
                .body(season);
    }

    @PutMapping("/{id}/seasons/{number}")
    @Operation(summary = "Modifica una temporada (ADMIN)")
    SeasonView updateSeason(
            @PathVariable UUID id, @PathVariable int number, @Valid @RequestBody SeasonUpdateCommand command) {
        return series.updateSeason(id, number, command);
    }

    @DeleteMapping("/{id}/seasons/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra una temporada sin episodios (ADMIN)")
    void deleteSeason(@PathVariable UUID id, @PathVariable int number) {
        series.deleteSeason(id, number);
    }

    @PostMapping("/{id}/seasons/{number}/episodes")
    @Operation(summary = "Agrega un episodio a una temporada (ADMIN)")
    ResponseEntity<EpisodeView> addEpisode(
            @PathVariable UUID id, @PathVariable int number, @Valid @RequestBody EpisodeCommand command) {
        EpisodeView episode = series.addEpisode(id, number, command);
        return ResponseEntity.created(URI.create("/api/v1/episodes/" + episode.id()))
                .body(episode);
    }
}
