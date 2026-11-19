package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.domain.Genre;
import com.amillioncodelines.streamstories.catalog.domain.GenreRepository;
import com.amillioncodelines.streamstories.shared.error.InvalidRequestException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Turns the genre ids of a request into genres, rejecting ids that do not exist. */
@Component
class GenreResolver {

    private final GenreRepository genres;

    GenreResolver(GenreRepository genres) {
        this.genres = genres;
    }

    List<Genre> resolve(Set<UUID> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }
        List<Genre> found = genres.findAllById(genreIds);
        if (found.size() != genreIds.size()) {
            Set<UUID> missing = new HashSet<>(genreIds);
            found.forEach(genre -> missing.remove(genre.getId()));
            throw new InvalidRequestException("These genres do not exist: %s."
                    .formatted(missing.stream().map(UUID::toString).sorted().collect(Collectors.joining(", "))));
        }
        return found;
    }
}
