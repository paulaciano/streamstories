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
            throw new ConflictException("La serie ya tiene una temporada %d.".formatted(number));
        }
        Season season = new Season(this, number, title);
        seasons.add(season);
        seasons.sort(Comparator.comparingInt(Season::getNumber));
        return season;
    }

    /** Only an empty season can be removed; its episodes have to be deleted first. */
    public void removeSeason(Season season) {
        if (!season.getEpisodes().isEmpty()) {
            throw new ConflictException(
                    "La temporada %d tiene episodios: hay que borrarlos primero.".formatted(season.getNumber()));
        }
        seasons.remove(season);
    }

    public Optional<Season> findSeason(int number) {
        return seasons.stream().filter(season -> season.getNumber() == number).findFirst();
    }

    public List<Season> getSeasons() {
        return List.copyOf(seasons);
    }
}
