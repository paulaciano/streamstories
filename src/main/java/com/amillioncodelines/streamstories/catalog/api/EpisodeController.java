package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/episodes")
@Tag(name = "Series", description = "Series, temporadas y alta de episodios")
class EpisodeController {

    private final SeriesService series;

    EpisodeController(SeriesService series) {
        this.series = series;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Detalle de un episodio",
            description = "`playable` indica si el episodio y su serie están disponibles.")
    EpisodeResponse get(@PathVariable UUID id, Authentication authentication) {
        return EpisodeResponse.from(series.getEpisode(id, Audiences.of(authentication)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplaza los datos de un episodio (ADMIN)")
    EpisodeResponse replace(@PathVariable UUID id, @Valid @RequestBody EpisodeRequest request) {
        return EpisodeResponse.from(series.replaceEpisode(id, request.toCommand()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Retira un episodio o lo vuelve a ofrecer (ADMIN)")
    EpisodeResponse changeAvailability(@PathVariable UUID id, @Valid @RequestBody AvailabilityRequest request) {
        return EpisodeResponse.from(series.changeEpisodeAvailability(id, request.availability()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra un episodio sin actividad de usuarios (ADMIN)")
    void delete(@PathVariable UUID id) {
        series.deleteEpisode(id);
    }
}
