package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.catalog.application.CatalogService;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import com.amillioncodelines.streamstories.catalog.domain.CatalogFilter;
import com.amillioncodelines.streamstories.catalog.domain.MediaType;
import com.amillioncodelines.streamstories.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Catalog", description = "Browsing the mixed catalog of movies and series")
class CatalogController {

    private final CatalogService catalog;

    CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    @Operation(summary = "Mixed catalog of movies and series, filterable and paginated")
    PageResponse<CatalogItemResponse> browse(
            @Parameter(description = "Genre slug, for example `sci-fi`") @RequestParam(required = false) String genre,
            @RequestParam(required = false) MediaType type,
            @Parameter(description = "Text to search for in the title") @RequestParam(required = false) String q,
            @Parameter(description = "ADMIN only. A USER always sees available content only.")
                    @RequestParam(required = false)
                    Availability availability,
            @ParameterObject Pageable pageable,
            Authentication authentication) {
        var filter = new CatalogFilter(genre, type, q, availability);
        var page = catalog.browse(filter, Audiences.of(authentication), TitleSorting.restrict(pageable));
        return PageResponse.of(page, CatalogItemResponse::from);
    }
}
