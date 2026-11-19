package com.amillioncodelines.streamstories.catalog.application;

import com.amillioncodelines.streamstories.catalog.domain.Availability;
import com.amillioncodelines.streamstories.catalog.domain.MediaItemRepository;
import com.amillioncodelines.streamstories.catalog.domain.Slugs;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

@Component
@Profile("dev")
class SampleCatalogLoader implements ApplicationRunner {

    static final String RESOURCE = "sample-data/catalog.json";

    private static final Logger log = LoggerFactory.getLogger(SampleCatalogLoader.class);
    private static final String MEDIA_BASE_URL = "https://cdn.example.com";

    private final MediaItemRepository titles;
    private final GenreService genres;
    private final MovieService movies;
    private final SeriesService series;
    private final JsonMapper json;
    private final TransactionTemplate transaction;

    SampleCatalogLoader(
            MediaItemRepository titles,
            GenreService genres,
            MovieService movies,
            SeriesService series,
            JsonMapper json,
            PlatformTransactionManager transactionManager) {
        this.titles = titles;
        this.genres = genres;
        this.movies = movies;
        this.series = series;
        this.json = json;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (titles.count() > 0) {
            log.info("The catalog already has titles; sample data not loaded");
            return;
        }
        int loaded = load();
        log.info("Sample catalog loaded: {} titles", loaded);
    }

    int load() {
        SampleCatalog catalog = read();
        return transaction.execute(status -> {
            Map<String, UUID> genreIds = new HashMap<>();
            catalog.genres()
                    .forEach(name -> genreIds.put(name, genres.create(name).id()));
            catalog.movies().forEach(movie -> loadMovie(movie, genreIds));
            catalog.series().forEach(item -> loadSeries(item, genreIds));
            return catalog.movies().size() + catalog.series().size();
        });
    }

    private void loadMovie(SampleMovie movie, Map<String, UUID> genreIds) {
        String slug = Slugs.from(movie.title());
        UUID id = movies.create(new MovieCommand(
                        movie.title(),
                        movie.synopsis(),
                        movie.releaseYear(),
                        movie.durationSeconds(),
                        videoUrl(slug),
                        coverUrl(slug),
                        ids(movie.genres(), genreIds)))
                .id();
        if (isTrue(movie.retired())) {
            movies.changeAvailability(id, Availability.RETIRED);
        }
    }

    private void loadSeries(SampleSeries item, Map<String, UUID> genreIds) {
        String slug = Slugs.from(item.title());
        UUID id = series.create(new SeriesCommand(
                        item.title(),
                        item.synopsis(),
                        item.releaseYear(),
                        coverUrl(slug),
                        ids(item.genres(), genreIds)))
                .id();
        for (int s = 0; s < item.seasons().size(); s++) {
            SampleSeason season = item.seasons().get(s);
            int seasonNumber = s + 1;
            series.addSeason(id, seasonNumber, season.title());
            for (int e = 0; e < season.episodes().size(); e++) {
                SampleEpisode episode = season.episodes().get(e);
                int episodeNumber = e + 1;
                series.addEpisode(
                        id,
                        seasonNumber,
                        new EpisodeCommand(
                                episodeNumber,
                                episode.title(),
                                episode.synopsis(),
                                episode.durationSeconds(),
                                videoUrl("%s-t%d-e%d".formatted(slug, seasonNumber, episodeNumber))));
            }
            if (isTrue(season.retired())) {
                series.changeSeasonAvailability(id, seasonNumber, Availability.RETIRED);
            }
        }
    }

    private SampleCatalog read() {
        try (InputStream input = new ClassPathResource(RESOURCE).getInputStream()) {
            return json.readValue(input, SampleCatalog.class);
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot read " + RESOURCE, ex);
        }
    }

    private static Set<UUID> ids(List<String> names, Map<String, UUID> genreIds) {
        return names.stream()
                .map(name -> {
                    UUID id = genreIds.get(name);
                    if (id == null) {
                        throw new IllegalStateException("Unknown genre in " + RESOURCE + ": " + name);
                    }
                    return id;
                })
                .collect(Collectors.toSet());
    }

    private static boolean isTrue(Boolean flag) {
        return Boolean.TRUE.equals(flag);
    }

    private static String videoUrl(String slug) {
        return MEDIA_BASE_URL + "/videos/" + slug + ".m3u8";
    }

    private static String coverUrl(String slug) {
        return MEDIA_BASE_URL + "/covers/" + slug + ".jpg";
    }

    record SampleCatalog(List<String> genres, List<SampleMovie> movies, List<SampleSeries> series) {}

    record SampleMovie(
            String title,
            String synopsis,
            int releaseYear,
            int durationSeconds,
            List<String> genres,
            Boolean retired) {}

    record SampleSeries(
            String title, String synopsis, int releaseYear, List<String> genres, List<SampleSeason> seasons) {}

    record SampleSeason(String title, Boolean retired, List<SampleEpisode> episodes) {}

    record SampleEpisode(String title, String synopsis, int durationSeconds) {}
}
