package com.amillioncodelines.streamstories.catalog.domain;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Translates a {@link CatalogFilter} into a query over titles. */
public final class MediaItemSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private MediaItemSpecifications() {}

    public static <T extends MediaItem> Specification<T> matching(CatalogFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.availability() != null) {
                predicates.add(cb.equal(root.get("availability"), filter.availability()));
            }
            if (filter.type() != null) {
                Class<? extends MediaItem> subtype = filter.type() == MediaType.MOVIE ? Movie.class : Series.class;
                predicates.add(cb.equal(root.type(), subtype));
            }
            if (filter.text() != null && !filter.text().isBlank()) {
                String pattern = "%" + escapeLike(filter.text().strip().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), pattern, LIKE_ESCAPE));
            }
            if (filter.genreSlug() != null && !filter.genreSlug().isBlank()) {
                // A subquery instead of a join, so a title never appears twice in a page.
                Subquery<Integer> classified = query.subquery(Integer.class);
                Root<MediaItem> item = classified.from(MediaItem.class);
                Join<MediaItem, Genre> genre = item.join("genres");
                classified
                        .select(cb.literal(1))
                        .where(cb.equal(item, root), cb.equal(genre.get("slug"), filter.genreSlug()));
                predicates.add(cb.exists(classified));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
