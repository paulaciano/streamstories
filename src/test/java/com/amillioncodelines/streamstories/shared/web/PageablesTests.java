package com.amillioncodelines.streamstories.shared.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amillioncodelines.streamstories.shared.error.InvalidRequestException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageablesTests {

    private static final Set<String> ALLOWED = Set.of("title", "releaseYear");
    private static final Sort DEFAULT_SORT = Sort.by("title");

    @Test
    void shouldApplyDefaultSortWhenNoneRequested() {
        Pageable result = Pageables.restrictSort(PageRequest.of(2, 10), ALLOWED, DEFAULT_SORT);

        assertThat(result.getSort()).isEqualTo(DEFAULT_SORT.and(Sort.by("id")));
        assertThat(result.getPageNumber()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(10);
    }

    @Test
    void shouldKeepRequestedSortAndBreakTiesById() {
        Sort requested = Sort.by(Sort.Direction.DESC, "releaseYear");

        Pageable result = Pageables.restrictSort(PageRequest.of(0, 20, requested), ALLOWED, DEFAULT_SORT);

        assertThat(result.getSort()).isEqualTo(requested.and(Sort.by("id")));
    }

    @Test
    void shouldRejectSortByPropertyNotAllowed() {
        Pageable request = PageRequest.of(0, 20, Sort.by("passwordHash"));

        assertThatThrownBy(() -> Pageables.restrictSort(request, ALLOWED, DEFAULT_SORT))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("passwordHash")
                .hasMessageContaining("releaseYear, title");
    }

    @Test
    void shouldRejectOffsetsBeyondTheLargestInteger() {
        assertThatThrownBy(() -> Pageables.restrictSort(PageRequest.of(30_000_000, 100), ALLOWED, DEFAULT_SORT))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> Pageables.fixedOrder(PageRequest.of(30_000_000, 100), DEFAULT_SORT))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldIgnoreTheRequestedSortWhenTheOrderIsFixed() {
        Pageable result = Pageables.fixedOrder(PageRequest.of(1, 10, Sort.by("passwordHash")), DEFAULT_SORT);

        assertThat(result.getSort()).isEqualTo(DEFAULT_SORT.and(Sort.by("id")));
        assertThat(result.getPageNumber()).isEqualTo(1);
    }
}
