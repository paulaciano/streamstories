package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.MovieView;
import com.amillioncodelines.streamstories.catalog.domain.Availability;
import com.amillioncodelines.streamstories.catalog.domain.CatalogFilter;
import com.amillioncodelines.streamstories.catalog.domain.MediaItemSpecifications;
import com.amillioncodelines.streamstories.catalog.domain.MediaType;
import com.amillioncodelines.streamstories.catalog.domain.Movie;
import com.amillioncodelines.streamstories.catalog.domain.MovieRepository;
import com.amillioncodelines.streamstories.catalog.domain.TitleDetails;
import com.amillioncodelines.streamstories.shared.error.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieService {

    private final MovieRepository movies;
    private final GenreResolver genres;
    private final ContentActivityGuard activity;

    MovieService(MovieRepository movies, GenreResolver genres, ContentActivityGuard activity) {
        this.movies = movies;
        this.genres = genres;
        this.activity = activity;
    }

    @Transactional(readOnly = true)
    public Page<MovieView> list(CatalogFilter filter, Audience audience, Pageable pageable) {
        CatalogFilter movieFilter = CatalogService.forAudience(
                new CatalogFilter(filter.genreSlug(), MediaType.MOVIE, filter.text(), filter.availability()), audience);
        return movies.findAll(MediaItemSpecifications.matching(movieFilter), pageable)
                .map(movie -> MovieView.of(movie, audience));
    }

    @Transactional(readOnly = true)
    public MovieView get(UUID id, Audience audience) {
        return MovieView.of(find(id), audience);
    }

    @Transactional
    public MovieView create(MovieCommand command) {
        Movie movie = new Movie(
                details(command), genres.resolve(command.genreIds()), command.durationSeconds(), command.videoUrl());
        return MovieView.of(movies.saveAndFlush(movie), Audience.ADMIN);
    }

    @Transactional
    public MovieView replace(UUID id, MovieCommand command) {
        return replace(id, command, null);
    }

    @Transactional
    public MovieView replace(UUID id, MovieCommand command, Long expectedVersion) {
        Movie movie = find(id);
        movie.requireVersion(expectedVersion);
        movie.update(details(command), genres.resolve(command.genreIds()));
        movie.changeMedia(command.durationSeconds(), command.videoUrl());
        return MovieView.of(movies.saveAndFlush(movie), Audience.ADMIN);
    }

    @Transactional
    public MovieView changeAvailability(UUID id, Availability availability) {
        return changeAvailability(id, availability, null);
    }

    @Transactional
    public MovieView changeAvailability(UUID id, Availability availability, Long expectedVersion) {
        Movie movie = find(id);
        movie.requireVersion(expectedVersion);
        movie.changeAvailability(availability);
        return MovieView.of(movies.saveAndFlush(movie), Audience.ADMIN);
    }

    @Transactional
    public void delete(UUID id) {
        Movie movie = find(id);
        activity.ensureNoActivity(ContentKind.MOVIE, id);
        movies.delete(movie);
    }

    private Movie find(UUID id) {
        return movies.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie %s does not exist.".formatted(id)));
    }

    private static TitleDetails details(MovieCommand command) {
        return new TitleDetails(command.title(), command.synopsis(), command.releaseYear(), command.coverImageUrl());
    }
}
