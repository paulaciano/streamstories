package com.amillioncodelines.streamstories.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amillioncodelines.streamstories.shared.error.ConflictException;
import java.util.List;
import org.junit.jupiter.api.Test;

class SeriesTests {

    private static final EpisodeDetails PILOT = new EpisodeDetails("Pilot", null, 2700, "https://cdn.example.com/e1");

    private final Series series = new Series(new TitleDetails("Dark", null, 2017, null), List.of());

    @Test
    void shouldKeepSeasonsOrderedAndRejectDuplicateNumbers() {
        series.addSeason(2, null);
        series.addSeason(1, "Origins");

        assertThat(series.getSeasons()).extracting(Season::getNumber).containsExactly(1, 2);
        assertThatThrownBy(() -> series.addSeason(1, null)).isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldRejectSeasonsAndEpisodesNumberedBelowOne() {
        assertThatThrownBy(() -> series.addSeason(0, null)).isInstanceOf(IllegalArgumentException.class);
        Season season = series.addSeason(1, null);
        assertThatThrownBy(() -> season.addEpisode(0, PILOT)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectDuplicateEpisodeNumbersWithinASeason() {
        Season season = series.addSeason(1, null);
        Episode pilot = season.addEpisode(1, PILOT);
        Episode second = season.addEpisode(2, PILOT);

        assertThatThrownBy(() -> season.addEpisode(1, PILOT)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> season.updateEpisode(second, 1, PILOT)).isInstanceOf(ConflictException.class);
        season.updateEpisode(pilot, 1, new EpisodeDetails("Renamed pilot", null, 2600, "https://cdn.example.com/e1"));
        assertThat(pilot.getTitle()).isEqualTo("Renamed pilot");
    }

    @Test
    void shouldOnlyRemoveEmptySeasons() {
        Season season = series.addSeason(1, null);
        Episode pilot = season.addEpisode(1, PILOT);

        assertThatThrownBy(() -> series.removeSeason(season)).isInstanceOf(ConflictException.class);

        season.removeEpisode(pilot);
        series.removeSeason(season);
        assertThat(series.getSeasons()).isEmpty();
    }

    @Test
    void shouldMakeEpisodesUnplayableWhenTheEpisodeOrItsSeriesIsRetired() {
        Episode pilot = series.addSeason(1, null).addEpisode(1, PILOT);
        assertThat(pilot.isPlayable()).isTrue();

        series.changeAvailability(Availability.RETIRED);
        assertThat(pilot.isPlayable()).isFalse();
        assertThat(pilot.isAvailable()).isTrue();

        series.changeAvailability(Availability.AVAILABLE);
        pilot.changeAvailability(Availability.RETIRED);
        assertThat(pilot.isPlayable()).isFalse();
    }
}
