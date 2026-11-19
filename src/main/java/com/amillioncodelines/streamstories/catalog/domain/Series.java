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

    public Season addSeason(int number, String title) {
        if (findSeason(number).isPresent()) {
            throw new ConflictException("The series already has a season %d.".formatted(number));
        }
        Season season = new Season(this, number, title);
        seasons.add(season);
        seasons.sort(Comparator.comparingInt(Season::getNumber));
        return season;
    }

    public void removeSeason(Season season) {
        if (!season.getEpisodes().isEmpty()) {
            throw new ConflictException("Season %d has episodes: delete them first.".formatted(season.getNumber()));
        }
        seasons.remove(season);
    }

    public Optional<Season> findSeason(int number) {
        return seasons.stream().filter(season -> season.getNumber() == number).findFirst();
    }

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
