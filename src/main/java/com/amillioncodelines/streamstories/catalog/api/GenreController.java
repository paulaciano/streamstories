package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.GenreView;
import com.amillioncodelines.streamstories.catalog.application.GenreCommand;
import com.amillioncodelines.streamstories.catalog.application.GenreService;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import com.amillioncodelines.streamstories.shared.web.Pageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/genres")
@Tag(name = "Géneros")
class GenreController {

    private static final Set<String> SORTABLE = Set.of("name");
    private static final Sort DEFAULT_SORT = Sort.by("name");

    private final GenreService genres;

    GenreController(GenreService genres) {
        this.genres = genres;
    }

    @GetMapping
    @Operation(summary = "Lista de géneros")
    PageResponse<GenreView> list(@ParameterObject Pageable pageable) {
        return PageResponse.of(genres.list(Pageables.restrictSort(pageable, SORTABLE, DEFAULT_SORT)), genre -> genre);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un género")
    GenreView get(@PathVariable UUID id) {
        return genres.get(id);
    }

    @PostMapping
    @Operation(summary = "Crea un género (ADMIN). El slug se genera a partir del nombre.")
    ResponseEntity<GenreView> create(@Valid @RequestBody GenreCommand command) {
        GenreView genre = genres.create(command);
        return ResponseEntity.created(URI.create("/api/v1/genres/" + genre.id()))
                .body(genre);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cambia el nombre de un género (ADMIN)")
    GenreView rename(@PathVariable UUID id, @Valid @RequestBody GenreCommand command) {
        return genres.rename(id, command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borra un género sin títulos asociados (ADMIN)")
    void delete(@PathVariable UUID id) {
        genres.delete(id);
    }
}
