package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.ContentKind;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.EpisodeView;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeasonView;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeriesSummaryView;
import com.amillioncodelines.streamstories.catalog.application.CatalogViews.SeriesView;
import com.amillioncodelines.streamstories.catalog.domain.CatalogFilter;
import com.amillioncodelines.streamstories.catalog.domain.Episode;
import com.amillioncodelines.streamstories.catalog.domain.EpisodeDetails;
import com.amillioncodelines.streamstories.catalog.domain.EpisodeRepository;
import com.amillioncodelines.streamstories.catalog.domain.MediaItemSpecifications;
import com.amillioncodelines.streamstories.catalog.domain.MediaType;
import com.amillioncodelines.streamstories.catalog.domain.Season;
import com.amillioncodelines.streamstories.catalog.domain.Series;
import com.amillioncodelines.streamstories.catalog.domain.SeriesRepository;
import com.amillioncodelines.streamstories.catalog.domain.TitleDetails;
import com.amillioncodelines.streamstories.shared.error.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Series together with their seasons and episodes, which only exist inside a series. */
@Service
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final EpisodeRepository episodes;
    private final GenreResolver genres;
    private final ContentActivityGuard activity;

    SeriesService(
            SeriesRepository seriesRepository,
            EpisodeRepository episodes,
            GenreResolver genres,
            ContentActivityGuard activity) {
        this.seriesRepository = seriesRepository;
        this.episodes = episodes;
        this.genres = genres;
        this.activity = activity;
    }

    // Series

    @Transactional(readOnly = true)
    public Page<SeriesSummaryView> list(CatalogFilter filter, Audience audience, Pageable pageable) {
        CatalogFilter seriesFilter = CatalogService.forAudience(
                new CatalogFilter(filter.genreSlug(), MediaType.SERIES, filter.text(), filter.availability()),
                audience);
        return seriesRepository
                .findAll(MediaItemSpecifications.matching(seriesFilter), pageable)
                .map(SeriesSummaryView::of);
    }

    @Transactional(readOnly = true)
    public SeriesView get(UUID id, Audience audience) {
        return SeriesView.of(findSeries(id), audience);
    }

    @Transactional
    public SeriesView create(SeriesCommand command) {
        Series series = new Series(details(command), genres.resolve(command.genreIds()));
        return SeriesView.of(seriesRepository.saveAndFlush(series), Audience.ADMIN);
    }

    @Transactional
    public SeriesView replace(UUID id, SeriesCommand command) {
        Series series = findSeries(id);
        series.update(details(command), genres.resolve(command.genreIds()));
        return SeriesView.of(seriesRepository.saveAndFlush(series), Audience.ADMIN);
    }

    /** Retiring a series makes all its episodes unplayable, without touching them. */
    @Transactional
    public SeriesView changeAvailability(UUID id, AvailabilityCommand command) {
        Series series = findSeries(id);
        series.changeAvailability(command.availability());
        return SeriesView.of(seriesRepository.saveAndFlush(series), Audience.ADMIN);
    }

    /** Deletes the series with its seasons and episodes, only if none of them has activity (rule C11). */
    @Transactional
    public void delete(UUID id) {
        Series series = findSeries(id);
        activity.ensureNoActivity(ContentKind.SERIES, id);
        series.getSeasons().stream()
                .flatMap(season -> season.getEpisodes().stream())
                .forEach(episode -> activity.ensureNoActivity(ContentKind.EPISODE, episode.getId()));
        seriesRepository.delete(series);
    }

    // Seasons

    @Transactional(readOnly = true)
    public SeasonView getSeason(UUID seriesId, int number, Audience audience) {
        return SeasonView.of(findSeason(findSeries(seriesId), number), audience);
    }

    /** Season numbers are unique within the series and start at 1 (rule C4). */
    @Transactional
    public SeasonView addSeason(UUID seriesId, SeasonCommand command) {
        Series series = findSeries(seriesId);
        Season season = series.addSeason(command.number(), command.title());
        // The series is managed: flushing persists the new season itself. A merge (save) would persist a copy.
        seriesRepository.flush();
        return SeasonView.of(season, Audience.ADMIN);
    }

    @Transactional
    public SeasonView updateSeason(UUID seriesId, int number, SeasonUpdateCommand command) {
        Season season = findSeason(findSeries(seriesId), number);
        season.rename(command.title());
        return SeasonView.of(season, Audience.ADMIN);
    }

    /** Only an empty season can be deleted. */
    @Transactional
    public void deleteSeason(UUID seriesId, int number) {
        Series series = findSeries(seriesId);
        series.removeSeason(findSeason(series, number));
    }

    // Episodes

    @Transactional(readOnly = true)
    public EpisodeView getEpisode(UUID id, Audience audience) {
        return EpisodeView.of(findEpisode(id), audience);
    }

    /** Episode numbers are unique within the season and start at 1 (rule C5). */
    @Transactional
    public EpisodeView addEpisode(UUID seriesId, int seasonNumber, EpisodeCommand command) {
        Series series = findSeries(seriesId);
        Season season = findSeason(series, seasonNumber);
        Episode episode = season.addEpisode(command.number(), details(command));
        seriesRepository.flush();
        return EpisodeView.of(episode, Audience.ADMIN);
    }

    @Transactional
    public EpisodeView replaceEpisode(UUID id, EpisodeCommand command) {
        Episode episode = findEpisode(id);
        episode.getSeason().updateEpisode(episode, command.number(), details(command));
        episodes.flush();
        return EpisodeView.of(episode, Audience.ADMIN);
    }

    @Transactional
    public EpisodeView changeEpisodeAvailability(UUID id, AvailabilityCommand command) {
        Episode episode = findEpisode(id);
        episode.changeAvailability(command.availability());
        return EpisodeView.of(episode, Audience.ADMIN);
    }

    /** Only episodes without activity can be deleted (rule C11). */
    @Transactional
    public void deleteEpisode(UUID id) {
        Episode episode = findEpisode(id);
        activity.ensureNoActivity(ContentKind.EPISODE, id);
        episode.getSeason().removeEpisode(episode);
    }

    private Series findSeries(UUID id) {
        return seriesRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la serie %s.".formatted(id)));
    }

    private static Season findSeason(Series series, int number) {
        return series.findSeason(number)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La serie %s no tiene una temporada %d.".formatted(series.getId(), number)));
    }

    private Episode findEpisode(UUID id) {
        return episodes.findWithSeasonAndSeriesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el episodio %s.".formatted(id)));
    }

    private static TitleDetails details(SeriesCommand command) {
        return new TitleDetails(command.title(), command.synopsis(), command.releaseYear(), command.coverImageUrl());
    }

    private static EpisodeDetails details(EpisodeCommand command) {
        return new EpisodeDetails(command.title(), command.synopsis(), command.durationSeconds(), command.videoUrl());
    }
}
