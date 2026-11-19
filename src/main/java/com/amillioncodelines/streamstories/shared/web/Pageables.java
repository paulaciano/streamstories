package com.amillioncodelines.streamstories.shared.web;

import com.amillioncodelines.streamstories.shared.error.InvalidRequestException;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        checkOffset(pageable);
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

    /**
     * For lists with a fixed order: any requested sort is ignored, and the identifier breaks ties.
     *
     * @throws InvalidRequestException if the page starts beyond the largest supported offset
     */
    public static Pageable fixedOrder(Pageable pageable, Sort sort) {
        checkOffset(pageable);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(TIE_BREAKER));
    }

    /** Cuts a page from a list that is already in its final order, for results computed in memory. */
    public static <T> Page<T> slice(List<T> all, Pageable pageable) {
        int from = (int) Math.min(pageable.getOffset(), all.size());
        int to = Math.min(from + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(from, to), pageable, all.size());
    }

    /** The database offset is an int: a larger one would fail deep inside Spring Data instead of answering 400. */
    private static void checkOffset(Pageable pageable) {
        if (pageable.getOffset() + pageable.getPageSize() > Integer.MAX_VALUE) {
            throw new InvalidRequestException(
                    "The page is too far: page × size cannot exceed %d.".formatted(Integer.MAX_VALUE));
        }
    }
}
