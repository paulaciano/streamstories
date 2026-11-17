package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.MovieService;
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
@Tag(name = "Películas")
class MovieController {

    private final MovieService movies;

    MovieController(MovieService movies) {
        this.movies = movies;
    }

    @GetMapping
    @Operation(summary = "Lista películas, filtrables por género y texto")
    PageResponse<MovieResponse> list(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String q,
            @Parameter(description = "Solo para ADMIN") @RequestParam(required = false) Availability availability,
            @ParameterObject Pageable pageable,
            Authentication authentication) {
        var filter = new CatalogFilter(genre, null, q, availability);
        var page = movies.list(filter, Audiences.of(authentication), TitleSorting.restrict(pageable));
        return PageResponse.of(page, MovieResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Detalle de una película",
            description = "Una película retirada sigue accesible por id, sin URL de video para un USER.")
    MovieResponse get(@PathVariable UUID id, Authentication authentication) {
        return MovieResponse.from(movies.get(id, Audiences.of(authentication)));
    }

    @PostMapping
    @Operation(summary = "Crea una película (ADMIN)")
    ResponseEntity<MovieResponse> create(@Valid @RequestBody MovieRequest request) {
        MovieResponse movie = MovieResponse.from(movies.create(request.toCommand()));
        return ResponseEntity.created(URI.create("/api/v1/movies/" + movie.id()))
                .body(movie);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplaza los datos de una película, incluidos sus géneros (ADMIN)")
    MovieResponse replace(@PathVariable UUID id, @Valid @RequestBody MovieRequest request) {
        return MovieResponse.from(movies.replace(id, request.toCommand()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Retira una película o la vuelve a ofrecer (ADMIN)")
    MovieResponse changeAvailability(@PathVariable UUID id, @Valid @RequestBody AvailabilityRequest request) {
        return MovieResponse.from(movies.changeAvailability(id, request.availability()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra una película sin actividad de usuarios (ADMIN)")
    void delete(@PathVariable UUID id) {
        movies.delete(id);
    }
}
