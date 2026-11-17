package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.domain.Availability;
import com.amillioncodelines.streamstories.catalog.domain.Episode;
import com.amillioncodelines.streamstories.catalog.domain.Genre;
import com.amillioncodelines.streamstories.catalog.domain.MediaItem;
import com.amillioncodelines.streamstories.catalog.domain.MediaType;
import com.amillioncodelines.streamstories.catalog.domain.Movie;
import com.amillioncodelines.streamstories.catalog.domain.Season;
import com.amillioncodelines.streamstories.catalog.domain.Series;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Read-only views returned by the catalog. Video URLs are only included when the content can be played, or for
 * administrators.
 */
public final class CatalogViews {

    private CatalogViews() {}

    public record GenreView(UUID id, String name, String slug) {

        static GenreView of(Genre genre) {
            return new GenreView(genre.getId(), genre.getName(), genre.getSlug());
        }
    }

    /** A title in a mixed listing of movies and series. */
    public record CatalogItemView(
            UUID id,
            MediaType type,
            String title,
            int releaseYear,
            String coverImageUrl,
            Availability availability,
            List<GenreView> genres) {

        static CatalogItemView of(MediaItem item) {
            return new CatalogItemView(
                    item.getId(),
                    item.getType(),
                    item.getTitle(),
                    item.getReleaseYear(),
                    item.getCoverImageUrl(),
                    item.getAvailability(),
                    genreViews(item));
        }
    }

    public record MovieView(
            UUID id,
            String title,
            String synopsis,
            int releaseYear,
            int durationSeconds,
            String videoUrl,
            String coverImageUrl,
            Availability availability,
            List<GenreView> genres,
            Instant createdAt,
            Instant updatedAt) {

        static MovieView of(Movie movie, Audience audience) {
            boolean showVideo = movie.isPlayable() || audience.seesRetiredContent();
            return new MovieView(
                    movie.getId(),
                    movie.getTitle(),
                    movie.getSynopsis(),
                    movie.getReleaseYear(),
                    movie.getDurationSeconds(),
                    showVideo ? movie.getVideoUrl() : null,
                    movie.getCoverImageUrl(),
                    movie.getAvailability(),
                    genreViews(movie),
                    movie.getCreatedAt(),
                    movie.getUpdatedAt());
        }
    }

    /** A series without its seasons, as shown in listings. */
    public record SeriesSummaryView(
            UUID id,
            String title,
            String synopsis,
            int releaseYear,
            String coverImageUrl,
            Availability availability,
            List<GenreView> genres) {

        static SeriesSummaryView of(Series series) {
            return new SeriesSummaryView(
                    series.getId(),
                    series.getTitle(),
                    series.getSynopsis(),
                    series.getReleaseYear(),
                    series.getCoverImageUrl(),
                    series.getAvailability(),
                    genreViews(series));
        }
    }

    public record SeasonSummaryView(int number, String title, Availability availability, int episodeCount) {}

    public record SeriesView(
            UUID id,
            String title,
            String synopsis,
            int releaseYear,
            String coverImageUrl,
            Availability availability,
            List<GenreView> genres,
            List<SeasonSummaryView> seasons,
            Instant createdAt,
            Instant updatedAt) {

        static SeriesView of(Series series, Audience audience) {
            List<SeasonSummaryView> seasons = series.getSeasons().stream()
                    .filter(season -> audience.seesRetiredContent() || season.isAvailable())
                    .map(season -> new SeasonSummaryView(
                            season.getNumber(),
                            season.getTitle(),
                            season.getAvailability(),
                            season.getEpisodes().size()))
                    .toList();
            return new SeriesView(
                    series.getId(),
                    series.getTitle(),
                    series.getSynopsis(),
                    series.getReleaseYear(),
                    series.getCoverImageUrl(),
                    series.getAvailability(),
                    genreViews(series),
                    seasons,
                    series.getCreatedAt(),
                    series.getUpdatedAt());
        }
    }

    public record EpisodeSummaryView(UUID id, int number, String title, int durationSeconds) {}

    /** A season is reachable by its number even when retired, flagged as such (rule C9). */
    public record SeasonView(
            UUID seriesId, int number, String title, Availability availability, List<EpisodeSummaryView> episodes) {

        static SeasonView of(Season season) {
            List<EpisodeSummaryView> episodes = season.getEpisodes().stream()
                    .map(episode -> new EpisodeSummaryView(
                            episode.getId(), episode.getNumber(), episode.getTitle(), episode.getDurationSeconds()))
                    .toList();
            return new SeasonView(
                    season.getSeries().getId(),
                    season.getNumber(),
                    season.getTitle(),
                    season.getAvailability(),
                    episodes);
        }
    }

    /**
     * @param playable whether the episode's season and series are both available
     */
    public record EpisodeView(
            UUID id,
            UUID seriesId,
            String seriesTitle,
            int seasonNumber,
            int number,
            String title,
            String synopsis,
            int durationSeconds,
            String videoUrl,
            boolean playable) {

        static EpisodeView of(Episode episode, Audience audience) {
            Series series = episode.getSeason().getSeries();
            boolean showVideo = episode.isPlayable() || audience.seesRetiredContent();
            return new EpisodeView(
                    episode.getId(),
                    series.getId(),
                    series.getTitle(),
                    episode.getSeason().getNumber(),
                    episode.getNumber(),
                    episode.getTitle(),
                    episode.getSynopsis(),
                    episode.getDurationSeconds(),
                    showVideo ? episode.getVideoUrl() : null,
                    episode.isPlayable());
        }
    }

    private static List<GenreView> genreViews(MediaItem item) {
        return item.getGenres().stream()
                .sorted(Comparator.comparing(Genre::getName))
                .map(GenreView::of)
                .toList();
    }
}
