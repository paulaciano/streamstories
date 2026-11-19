package com.amillioncodelines.streamstories.catalog.domain;

import com.amillioncodelines.streamstories.shared.error.ConflictException;
import com.amillioncodelines.streamstories.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Entity
public class Franchise extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "franchise")
    @OrderBy("franchiseOrder")
    private List<Movie> movies = new ArrayList<>();

    protected Franchise() {}

    public Franchise(String name) {
        rename(name);
    }

    public final void rename(String newName) {
        this.name = newName.strip();
    }

    /** The order of the list is the order of the franchise. */
    public void replaceMovies(List<Movie> newMovies) {
        if (new HashSet<>(newMovies).size() != newMovies.size()) {
            throw new IllegalArgumentException("A movie appears more than once");
        }
        for (Movie movie : newMovies) {
            if (movie.getFranchise() != null && !equals(movie.getFranchise())) {
                throw new ConflictException("Movie %s already belongs to franchise '%s'."
                        .formatted(movie.getId(), movie.getFranchise().getName()));
            }
        }
        movies.stream().filter(movie -> !newMovies.contains(movie)).forEach(Movie::leaveFranchise);
        movies.clear();
        for (int position = 0; position < newMovies.size(); position++) {
            Movie movie = newMovies.get(position);
            movie.joinFranchise(this, position + 1);
            movies.add(movie);
        }
    }

    public void removeAllMovies() {
        movies.forEach(Movie::leaveFranchise);
        movies.clear();
    }

    public String getName() {
        return name;
    }

    public List<Movie> getMovies() {
        return List.copyOf(movies);
    }
}
