package com.amillioncodelines.streamstories.catalog.domain;

import com.amillioncodelines.streamstories.shared.error.ConflictException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** A series: a title made of numbered seasons. It can exist without seasons, for example while it is loaded (C7). */
@Entity
@DiscriminatorValue("SERIES")
public class Series extends MediaItem {

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("number")
    private List<Season> seasons = new ArrayList<>();

    protected Series() {}

    public Series(TitleDetails details, Collection<Genre> genres) {
        super(details, genres);
    }

    @Override
    public MediaType getType() {
        return MediaType.SERIES;
    }

    /** Season numbers are unique within the series (rule C4). */
    public Season addSeason(int number, String title) {
        if (findSeason(number).isPresent()) {
            throw new ConflictException("The series already has a season %d.".formatted(number));
        }
        Season season = new Season(this, number, title);
        seasons.add(season);
        seasons.sort(Comparator.comparingInt(Season::getNumber));
        return season;
    }

    /** Only an empty season can be removed; its episodes have to be deleted first. */
    public void removeSeason(Season season) {
        if (!season.getEpisodes().isEmpty()) {
            throw new ConflictException("Season %d has episodes: delete them first.".formatted(season.getNumber()));
        }
        seasons.remove(season);
    }

    public Optional<Season> findSeason(int number) {
        return seasons.stream().filter(season -> season.getNumber() == number).findFirst();
    }

    /**
     * Whether some playable episode comes after the given one, in season and episode order. Episodes of retired
     * seasons are skipped, and nothing is playable in a retired series.
     */
    public boolean hasPlayableEpisodeAfter(Episode episode) {
        if (!isAvailable()) {
            return false;
        }
        int seasonNumber = episode.getSeason().getNumber();
        return seasons.stream()
                .filter(Season::isAvailable)
                .anyMatch(season -> season.getNumber() > seasonNumber
                        ? !season.getEpisodes().isEmpty()
                        : season.getNumber() == seasonNumber
                                && season.getEpisodes().stream()
                                        .anyMatch(other -> other.getNumber() > episode.getNumber()));
    }

    public List<Season> getSeasons() {
        return List.copyOf(seasons);
    }
}
