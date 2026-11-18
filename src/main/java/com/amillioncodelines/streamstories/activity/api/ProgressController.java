package com.amillioncodelines.streamstories.activity.api;

import com.amillioncodelines.streamstories.activity.application.ProgressService;
import com.amillioncodelines.streamstories.identity.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/progress")
@Tag(name = "Actividad", description = "Progreso de reproducción del usuario actual")
class ProgressController {

    private static final String DESCRIPTION =
            "Guarda la posición exacta. Una posición mayor que la duración se recorta,"
                    + " y el contenido queda visto al llegar al 90 %, aunque después se reporte una posición menor."
                    + " Responde 409 si el contenido está retirado.";

    private final ProgressService progress;

    ProgressController(ProgressService progress) {
        this.progress = progress;
    }

    @PutMapping("/movies/{movieId}")
    @Operation(summary = "Reporta la posición de reproducción de una película", description = DESCRIPTION)
    ProgressResponse reportMovie(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID movieId, @Valid @RequestBody ProgressRequest request) {
        return ProgressResponse.from(progress.reportMovie(CurrentUser.id(jwt), movieId, request.positionSeconds()));
    }

    @PutMapping("/episodes/{episodeId}")
    @Operation(summary = "Reporta la posición de reproducción de un episodio", description = DESCRIPTION)
    ProgressResponse reportEpisode(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID episodeId,
            @Valid @RequestBody ProgressRequest request) {
        return ProgressResponse.from(progress.reportEpisode(CurrentUser.id(jwt), episodeId, request.positionSeconds()));
    }
}
