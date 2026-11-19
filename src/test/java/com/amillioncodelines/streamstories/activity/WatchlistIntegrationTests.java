package com.amillioncodelines.streamstories.activity;

import static com.amillioncodelines.streamstories.support.CatalogApi.token;
import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@IntegrationTest
class WatchlistIntegrationTests {

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldListSavedTitlesMostRecentlyAddedFirst() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Saved " + token(), 2000);
        String series = catalog.createSeries(admin, "Saved " + token(), 2000);

        assertThat(add(user, movie)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(add(user, series)).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(watchlist(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "type": "SERIES", "available": true},
                             {"titleId": "%s", "type": "MOVIE", "available": true}],
                 "page": {"totalElements": 2}}
                """.formatted(series, movie));
    }

    @Test
    void shouldKeepTheOriginalDateWhenATitleIsAddedTwice() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Saved " + token(), 2000);
        add(user, movie);
        String addedAt = ApiClient.read(watchlist(user), "$.content[0].addedAt");

        assertThat(add(user, movie)).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(watchlist(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "addedAt": "%s"}], "page": {"totalElements": 1}}
                """.formatted(movie, addedAt));
    }

    @Test
    void shouldNotAddRetiredTitlesButKeepTheOnesRetiredLater() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String kept = catalog.createMovie(admin, "Saved " + token(), 2000);
        String retired = catalog.createMovie(admin, "Saved " + token(), 2000);
        add(user, kept);
        catalog.changeAvailability(admin, "/api/v1/movies/" + kept, "RETIRED");
        catalog.changeAvailability(admin, "/api/v1/movies/" + retired, "RETIRED");

        assertThat(add(user, retired))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:content-unavailable");
        assertThat(watchlist(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "available": false}], "page": {"totalElements": 1}}
                """.formatted(kept));
    }

    @Test
    void shouldRemoveTitlesEvenWhenTheyAreNotThere() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Saved " + token(), 2000);
        add(user, movie);

        assertThat(catalog.delete(user, "/api/v1/me/watchlist/" + movie)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(catalog.delete(user, "/api/v1/me/watchlist/" + movie)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(watchlist(user))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
    }

    @Test
    void shouldOnlySaveMoviesAndSeries() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = catalog.createSeries(admin, "Saved " + token(), 2000);
        catalog.addSeason(admin, series, 1);
        String episode = catalog.addEpisode(admin, series, 1, 1);

        assertThat(add(user, episode)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteTitlesThatAreInAWatchlist() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Saved " + token(), 2000);
        String series = catalog.createSeries(admin, "Saved " + token(), 2000);
        add(user, movie);
        add(user, series);

        assertThat(catalog.delete(admin, "/api/v1/movies/" + movie)).hasStatus(HttpStatus.CONFLICT);
        assertThat(catalog.delete(admin, "/api/v1/series/" + series)).hasStatus(HttpStatus.CONFLICT);
    }

    private MvcTestResult add(Session user, String titleId) {
        return catalog.send(user, "PUT", "/api/v1/me/watchlist/" + titleId, "");
    }

    private MvcTestResult watchlist(Session user) {
        return catalog.get(user, "/api/v1/me/watchlist");
    }
}
