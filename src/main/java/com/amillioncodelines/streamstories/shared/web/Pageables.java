package com.amillioncodelines.streamstories.shared.web;

import com.amillioncodelines.streamstories.shared.error.InvalidRequestException;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Helpers to keep client-controlled pagination within the documented limits. */
public final class Pageables {

    private static final Sort TIE_BREAKER = Sort.by("id");

    private Pageables() {}

    /**
     * Validates that every sort property is in the allowed list and applies a default sort when none was requested.
     * The identifier is always added as the last criterion, so pages are stable when values tie.
     *
     * @throws InvalidRequestException if the request sorts by a property that is not allowed
     */
    public static Pageable restrictSort(Pageable pageable, Set<String> allowedProperties, Sort defaultSort) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new InvalidRequestException("Cannot sort by '%s'. Allowed fields: %s."
                        .formatted(
                                order.getProperty(),
                                String.join(
                                        ", ",
                                        allowedProperties.stream().sorted().toList())));
            }
        }
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : defaultSort;
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(TIE_BREAKER));
    }
}
