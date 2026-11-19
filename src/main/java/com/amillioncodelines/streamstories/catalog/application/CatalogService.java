package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.application.CatalogViews.CatalogItemView;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import com.amillioncodelines.streamstories.catalog.domain.CatalogFilter;
import com.amillioncodelines.streamstories.catalog.domain.MediaItemRepository;
import com.amillioncodelines.streamstories.catalog.domain.MediaItemSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final MediaItemRepository titles;

    CatalogService(MediaItemRepository titles) {
        this.titles = titles;
    }

    @Transactional(readOnly = true)
    public Page<CatalogItemView> browse(CatalogFilter filter, Audience audience, Pageable pageable) {
        return titles.findAll(MediaItemSpecifications.matching(forAudience(filter, audience)), pageable)
                .map(CatalogItemView::of);
    }

    static CatalogFilter forAudience(CatalogFilter filter, Audience audience) {
        if (audience.seesRetiredContent()) {
            return filter;
        }
        return new CatalogFilter(filter.genreSlug(), filter.type(), filter.text(), Availability.AVAILABLE);
    }
}
