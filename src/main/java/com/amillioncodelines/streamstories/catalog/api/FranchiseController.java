package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.FranchiseService;
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
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/v1/franchises")
@Tag(name = "Franchises", description = "Movies grouped in ordered franchises")
class FranchiseController {

    private static final Set<String> SORTABLE = Set.of("name");
    private static final Sort DEFAULT_SORT = Sort.by("name");

    private final FranchiseService franchises;

    FranchiseController(FranchiseService franchises) {
        this.franchises = franchises;
    }

    @GetMapping
    @Operation(summary = "Lists franchises")
    PageResponse<FranchiseResponse.Summary> list(@ParameterObject Pageable pageable) {
        return PageResponse.of(
                franchises.list(Pageables.restrictSort(pageable, SORTABLE, DEFAULT_SORT)),
                FranchiseResponse.Summary::from);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "A franchise with its movies in order",
            description = "A USER does not see retired movies; an ADMIN sees every movie.")
    FranchiseResponse get(@PathVariable UUID id, Authentication authentication) {
        return FranchiseResponse.from(franchises.get(id, Audiences.of(authentication)));
    }

    @PostMapping
    @Operation(
            summary = "Creates a franchise (ADMIN)",
            description =
                    "The order of movieIds is the order of the franchise. A movie belongs to one franchise at most.")
    ResponseEntity<FranchiseResponse> create(@Valid @RequestBody FranchiseRequest request) {
        FranchiseResponse franchise = FranchiseResponse.from(franchises.create(request.name(), request.movieIds()));
        return ResponseEntity.created(URI.create("/api/v1/franchises/" + franchise.id()))
                .body(franchise);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Replaces the name and the movies of a franchise (ADMIN)",
            description = "Movies left out of the new list leave the franchise.")
    FranchiseResponse replace(@PathVariable UUID id, @Valid @RequestBody FranchiseRequest request) {
        return FranchiseResponse.from(franchises.replace(id, request.name(), request.movieIds()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletes a franchise; its movies stay in the catalog (ADMIN)")
    void delete(@PathVariable UUID id) {
        franchises.delete(id);
    }
}
