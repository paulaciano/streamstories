package com.amillioncodelines.streamstories.catalog.domain;

import com.amillioncodelines.streamstories.shared.error.ConflictException;
import com.amillioncodelines.streamstories.shared.persistence.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A numbered season of a series, starting at 1. It can be retired as a whole, together with its episodes. */
@Entity
public class Season extends BaseEntity {

    /** Highest season or episode number. Numbers are stored as {@code smallint}, so they must stay in that range. */
    public static final int MAX_NUMBER = 999;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false, updatable = false)
    private Series series;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = false, updatable = false)
    private int number;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Availability availability = Availability.AVAILABLE;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number")
    private List<Episode> episodes = new ArrayList<>();

    protected Season() {}

    Season(Series series, int number, String title) {
        if (number < 1 || number > MAX_NUMBER) {
            throw new IllegalArgumentException("Season numbers go from 1 to " + MAX_NUMBER);
        }
        this.series = series;
        this.number = number;
        this.title = title;
    }

    public void rename(String newTitle) {
        this.title = newTitle;
    }

    /** Retires the whole season or offers it again. Episodes are never retired one by one. */
    public void changeAvailability(Availability newAvailability) {
        this.availability = Objects.requireNonNull(newAvailability);
    }

    public boolean isAvailable() {
        return availability == Availability.AVAILABLE;
    }

    public Availability getAvailability() {
        return availability;
    }

    /** Episode numbers are unique within the season (rule C5). */
    public Episode addEpisode(int episodeNumber, EpisodeDetails details) {
        ensureNumberIsFree(episodeNumber, null);
        Episode episode = new Episode(this, episodeNumber, details);
        episodes.add(episode);
        episodes.sort(Comparator.comparingInt(Episode::getNumber));
        return episode;
    }

    /** Changes the number and data of one of the season's episodes. */
    public void updateEpisode(Episode episode, int newNumber, EpisodeDetails details) {
        ensureNumberIsFree(newNumber, episode);
        episode.update(newNumber, details);
        episodes.sort(Comparator.comparingInt(Episode::getNumber));
    }

    public void removeEpisode(Episode episode) {
        episodes.remove(episode);
    }

    public Optional<Episode> findEpisode(int episodeNumber) {
        return episodes.stream()
                .filter(episode -> episode.getNumber() == episodeNumber)
                .findFirst();
    }

    private void ensureNumberIsFree(int episodeNumber, Episode except) {
        findEpisode(episodeNumber).filter(existing -> existing != except).ifPresent(existing -> {
            throw new ConflictException("La temporada %d ya tiene un episodio %d.".formatted(number, episodeNumber));
        });
    }

    public Series getSeries() {
        return series;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public List<Episode> getEpisodes() {
        return List.copyOf(episodes);
    }
}
