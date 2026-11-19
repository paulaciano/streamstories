package com.amillioncodelines.streamstories.catalog.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amillioncodelines.streamstories.catalog.ContentActivity;
import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.catalog.domain.Movie;
import com.amillioncodelines.streamstories.catalog.domain.MovieRepository;
import com.amillioncodelines.streamstories.catalog.domain.TitleDetails;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MovieServiceTests {

    private final MovieRepository movies = mock();
    private final ContentActivity activity = mock();
    private final MovieService service =
            new MovieService(movies, new GenreResolver(mock()), new ContentActivityGuard(List.of(activity)));

    @Test
    void shouldRefuseToDeleteAMovieWithActivity() {
        UUID id = UUID.randomUUID();
        Movie movie = new Movie(new TitleDetails("Arrival", null, 2016, null), List.of(), 6960, "https://v/1");
        when(movies.findById(id)).thenReturn(Optional.of(movie));
        when(activity.existsForAny(ContentKind.MOVIE, List.of(id))).thenReturn(true);

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(ConflictException.class);
        verify(movies, never()).delete(any(Movie.class));
    }

    @Test
    void shouldDeleteAMovieWithoutActivity() {
        UUID id = UUID.randomUUID();
        Movie movie = new Movie(new TitleDetails("Arrival", null, 2016, null), List.of(), 6960, "https://v/1");
        when(movies.findById(id)).thenReturn(Optional.of(movie));

        service.delete(id);

        verify(movies).delete(movie);
    }
}
