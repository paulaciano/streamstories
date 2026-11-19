package com.amillioncodelines.streamstories.catalog.api;

import com.amillioncodelines.streamstories.shared.web.Pageables;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class TitleSorting {

    private static final Set<String> SORTABLE = Set.of("title", "releaseYear", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by("title");

    private TitleSorting() {}

    static Pageable restrict(Pageable pageable) {
        return Pageables.restrictSort(pageable, SORTABLE, DEFAULT_SORT);
    }
}
