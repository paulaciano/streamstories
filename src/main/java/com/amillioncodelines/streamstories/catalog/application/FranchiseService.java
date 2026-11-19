package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.application.FranchiseView.FranchiseMovieView;
import com.amillioncodelines.streamstories.catalog.application.FranchiseView.FranchiseSummaryView;
import com.amillioncodelines.streamstories.catalog.domain.Franchise;
import com.amillioncodelines.streamstories.catalog.domain.FranchiseRepository;
import com.amillioncodelines.streamstories.catalog.domain.Movie;
import com.amillioncodelines.streamstories.catalog.domain.MovieRepository;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import com.amillioncodelines.streamstories.shared.error.InvalidRequestException;
import com.amillioncodelines.streamstories.shared.error.ResourceNotFoundException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FranchiseService {

    private final FranchiseRepository franchises;
    private final MovieRepository movies;

    FranchiseService(FranchiseRepository franchises, MovieRepository movies) {
        this.franchises = franchises;
        this.movies = movies;
    }

    @Transactional(readOnly = true)
    public Page<FranchiseSummaryView> list(Pageable pageable) {
        return franchises
                .findAll(pageable)
                .map(franchise -> new FranchiseSummaryView(franchise.getId(), franchise.getName()));
    }

    @Transactional(readOnly = true)
    public FranchiseView get(UUID id, Audience audience) {
        return view(find(id), audience);
    }

    @Transactional
    public FranchiseView create(String name, List<UUID> movieIds) {
        if (franchises.existsByNameIgnoreCase(name.strip())) {
            throw new ConflictException("Franchise '%s' already exists.".formatted(name.strip()));
        }
        Franchise franchise = franchises.save(new Franchise(name));
        franchise.replaceMovies(resolve(franchise, movieIds));
        return view(franchises.saveAndFlush(franchise), Audience.ADMIN);
    }

    @Transactional
    public FranchiseView replace(UUID id, String name, List<UUID> movieIds) {
        Franchise franchise = find(id);
        if (franchises.existsByNameIgnoreCaseAndIdNot(name.strip(), id)) {
            throw new ConflictException("Franchise '%s' already exists.".formatted(name.strip()));
        }
        franchise.rename(name);
        franchise.replaceMovies(resolve(franchise, movieIds));
        return view(franchises.saveAndFlush(franchise), Audience.ADMIN);
    }

    /** The movies stay in the catalog: they only leave the franchise. */
    @Transactional
    public void delete(UUID id) {
        Franchise franchise = find(id);
        franchise.removeAllMovies();
        franchises.delete(franchise);
    }

    private List<Movie> resolve(Franchise franchise, List<UUID> movieIds) {
        Set<UUID> distinct = new LinkedHashSet<>(movieIds);
        if (distinct.size() != movieIds.size()) {
            throw new InvalidRequestException("A movie cannot appear twice in a franchise.");
        }
        Map<UUID, Movie> found =
                movies.findAllById(distinct).stream().collect(Collectors.toMap(Movie::getId, Function.identity()));
        if (found.size() != distinct.size()) {
            Set<UUID> missing = new HashSet<>(distinct);
            missing.removeAll(found.keySet());
            throw new InvalidRequestException("These movies do not exist: %s."
                    .formatted(missing.stream().map(UUID::toString).sorted().collect(Collectors.joining(", "))));
        }
        franchises.findContainingAny(distinct).stream()
                .filter(other -> !other.equals(franchise))
                .findFirst()
                .ifPresent(other -> {
                    throw new ConflictException(
                            "A movie already belongs to franchise '%s'.".formatted(other.getName()));
                });
        return movieIds.stream().map(found::get).toList();
    }

    private Franchise find(UUID id) {
        return franchises
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise %s does not exist.".formatted(id)));
    }

    private static FranchiseView view(Franchise franchise, Audience audience) {
        List<Movie> ordered = franchise.getMovies();
        List<FranchiseMovieView> movies = IntStream.range(0, ordered.size())
                .filter(index ->
                        audience.seesRetiredContent() || ordered.get(index).isAvailable())
                .mapToObj(index -> new FranchiseMovieView(
                        ordered.get(index).getId(),
                        index + 1,
                        ordered.get(index).getTitle(),
                        ordered.get(index).getReleaseYear(),
                        ordered.get(index).getCoverImageUrl(),
                        ordered.get(index).getAvailability()))
                .toList();
        return new FranchiseView(
                franchise.getId(), franchise.getName(), movies, franchise.getCreatedAt(), franchise.getUpdatedAt());
    }
}
