package com.amillioncodelines.streamstories.activity;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * "Continue watching" and the history. Each test uses a new user, so the lists only contain what the test watched.
 * Episodes last 2700 seconds (completed from 2430) and movies 6000 (completed from 5400).
 */
@IntegrationTest
class WatchHistoryIntegrationTests {

    private static final int EPISODE_COMPLETED = 2430;
    private static final int MOVIE_COMPLETED = 5400;

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    // Continue watching

    @Test
    void shouldListUnfinishedMoviesMostRecentlyWatchedFirst() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String first = newMovie(admin);
        String second = newMovie(admin);
        String finished = newMovie(admin);
        watchMovie(user, first, 60);
        watchMovie(user, second, 120);
        watchMovie(user, finished, MOVIE_COMPLETED);

        assertThat(continueWatching(user))
                .bodyJson()
                .extractingPath("$.content[*].titleId")
                .asArray()
                .containsExactly(second, first);
        assertThat(continueWatching(user)).bodyJson().doesNotHavePath("$.content[0].episode");
    }

    @Test
    void shouldShowOneEntryPerSeriesWithTheLastWatchedEpisode() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = newSeries(admin);
        catalog.addSeason(admin, series, 1);
        String pilot = catalog.addEpisode(admin, series, 1, 1);
        String second = catalog.addEpisode(admin, series, 1, 2);
        watchEpisode(user, pilot, EPISODE_COMPLETED);
        watchEpisode(user, second, 300);

        assertThat(continueWatching(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"type": "SERIES", "titleId": "%s",
                              "episode": {"id": "%s", "seasonNumber": 1, "number": 2},
                              "positionSeconds": 300, "completed": false, "playable": true}],
                 "page": {"totalElements": 1}}
                """.formatted(series, second));
    }

    @Test
    void shouldKeepTheSeriesWhenTheLastWatchedEpisodeIsCompletedAndAnotherFollows() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = newSeries(admin);
        catalog.addSeason(admin, series, 1);
        String pilot = catalog.addEpisode(admin, series, 1, 1);
        catalog.addSeason(admin, series, 2);
        catalog.addEpisode(admin, series, 2, 1);
        watchEpisode(user, pilot, EPISODE_COMPLETED);

        assertThat(continueWatching(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "episode": {"id": "%s"}, "completed": true}]}
                """.formatted(series, pilot));
    }

    @Test
    void shouldDropTheSeriesWhenTheLastEpisodeIsCompleted() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = newSeries(admin);
        catalog.addSeason(admin, series, 1);
        catalog.addEpisode(admin, series, 1, 1);
        String finale = catalog.addEpisode(admin, series, 1, 2);
        watchEpisode(user, finale, EPISODE_COMPLETED);

        assertThat(continueWatching(user))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
    }

    @Test
    void shouldDropTheSeriesWhenOnlyRetiredSeasonsFollow() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = newSeries(admin);
        catalog.addSeason(admin, series, 1);
        String pilot = catalog.addEpisode(admin, series, 1, 1);
        catalog.addSeason(admin, series, 2);
        catalog.addEpisode(admin, series, 2, 1);
        watchEpisode(user, pilot, EPISODE_COMPLETED);

        catalog.changeAvailability(admin, "/api/v1/series/%s/seasons/2".formatted(series), "RETIRED");

        assertThat(continueWatching(user))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
    }

    @Test
    void shouldKeepAnAvailableSeriesWhenTheLastWatchedEpisodeIsInARetiredSeason() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = newSeries(admin);
        catalog.addSeason(admin, series, 1);
        catalog.addEpisode(admin, series, 1, 1);
        catalog.addSeason(admin, series, 2);
        String retired = catalog.addEpisode(admin, series, 2, 1);
        watchEpisode(user, retired, 300);

        catalog.changeAvailability(admin, "/api/v1/series/%s/seasons/2".formatted(series), "RETIRED");

        assertThat(continueWatching(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "episode": {"id": "%s"}, "playable": false}]}
                """.formatted(series, retired));
    }

    @Test
    void shouldLeaveOutRetiredMoviesAndSeries() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = newMovie(admin);
        String series = newSeries(admin);
        catalog.addSeason(admin, series, 1);
        String pilot = catalog.addEpisode(admin, series, 1, 1);
        watchMovie(user, movie, 60);
        watchEpisode(user, pilot, 60);

        catalog.changeAvailability(admin, "/api/v1/movies/" + movie, "RETIRED");
        catalog.changeAvailability(admin, "/api/v1/series/" + series, "RETIRED");

        assertThat(continueWatching(user))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
    }

    @Test
    void shouldPaginateContinueWatching() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String older = newMovie(admin);
        watchMovie(user, older, 60);
        watchMovie(user, newMovie(admin), 60);

        assertThat(catalog.get(user, "/api/v1/me/continue-watching?size=1&page=1"))
                .bodyJson()
                .isLenientlyEqualTo("""
                {"content": [{"titleId": "%s"}], "page": {"number": 1, "size": 1, "totalElements": 2, "totalPages": 2}}
                """.formatted(older));
    }

    // History

    @Test
    void shouldListEverythingWatchedIncludingRetiredContent() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String finished = newMovie(admin);
        String retired = newMovie(admin);
        String series = newSeries(admin);
        catalog.addSeason(admin, series, 1);
        String pilot = catalog.addEpisode(admin, series, 1, 1);
        watchMovie(user, finished, MOVIE_COMPLETED);
        watchMovie(user, retired, 60);
        watchEpisode(user, pilot, 600);
        catalog.changeAvailability(admin, "/api/v1/movies/" + retired, "RETIRED");

        assertThat(history(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [
                   {"type": "SERIES", "titleId": "%s", "episode": {"id": "%s", "seasonNumber": 1, "number": 1,
                    "title": "Episode 1"}, "positionSeconds": 600, "durationSeconds": 2700, "playable": true},
                   {"type": "MOVIE", "titleId": "%s", "playable": false},
                   {"type": "MOVIE", "titleId": "%s", "completed": true, "playable": true}],
                 "page": {"totalElements": 3}}
                """.formatted(series, pilot, retired, finished));
    }

    @Test
    void shouldCapSavedPositionsWhenTheDurationIsShortened() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String title = "Watched " + CatalogApi.token();
        String movie = catalog.createMovie(admin, title, 2000);
        watchMovie(user, movie, 5000);

        assertThat(catalog.send(admin, "PUT", "/api/v1/movies/" + movie, """
                        {"title": "%s", "releaseYear": 2000, "durationSeconds": 3000,
                         "videoUrl": "https://cdn.example.com/videos/shortened.m3u8"}
                        """.formatted(title)))
                .hasStatusOk();

        assertThat(history(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "positionSeconds": 3000, "durationSeconds": 3000, "completed": false}]}
                """.formatted(movie));
    }

    @Test
    void shouldPaginateTheHistory() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String older = newMovie(admin);
        watchMovie(user, older, MOVIE_COMPLETED);
        watchMovie(user, newMovie(admin), 60);

        assertThat(catalog.get(user, "/api/v1/me/history?size=1&page=1"))
                .bodyJson()
                .isLenientlyEqualTo("""
                {"content": [{"titleId": "%s"}], "page": {"number": 1, "totalElements": 2}}
                """.formatted(older));
    }

    private String newMovie(Session admin) {
        return catalog.createMovie(admin, "Watched " + CatalogApi.token(), 2000);
    }

    private String newSeries(Session admin) {
        return catalog.createSeries(admin, "Watched " + CatalogApi.token(), 2000);
    }

    private MvcTestResult continueWatching(Session user) {
        return catalog.get(user, "/api/v1/me/continue-watching");
    }

    private MvcTestResult history(Session user) {
        return catalog.get(user, "/api/v1/me/history");
    }

    private void watchMovie(Session user, String movieId, int positionSeconds) {
        watch(user, "/api/v1/me/progress/movies/" + movieId, positionSeconds);
    }

    private void watchEpisode(Session user, String episodeId, int positionSeconds) {
        watch(user, "/api/v1/me/progress/episodes/" + episodeId, positionSeconds);
    }

    private void watch(Session user, String uri, int positionSeconds) {
        assertThat(catalog.send(user, "PUT", uri, """
                        {"positionSeconds": %d}
                        """.formatted(positionSeconds)))
                .hasStatus(HttpStatus.OK);
    }
}
