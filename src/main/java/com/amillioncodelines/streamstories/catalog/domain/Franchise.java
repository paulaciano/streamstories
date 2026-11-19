package com.amillioncodelines.streamstories.catalog.domain;

import com.amillioncodelines.streamstories.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Entity
public class Franchise extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "franchise_movie",
            joinColumns = @JoinColumn(name = "franchise_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id"))
    @OrderColumn(name = "position")
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
        movies.clear();
        movies.addAll(newMovies);
    }

    public void removeMovie(Movie movie) {
        movies.remove(movie);
    }

    public void removeAllMovies() {
        movies.clear();
    }

    public String getName() {
        return name;
    }

    public List<Movie> getMovies() {
        return List.copyOf(movies);
    }
}
