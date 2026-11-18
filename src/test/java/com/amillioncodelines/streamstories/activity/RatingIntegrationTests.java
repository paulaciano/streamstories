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

@IntegrationTest
class RatingIntegrationTests {

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldRateATitleAndReplaceTheScore() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = newMovie(admin);

        assertThat(rate(user, movie, 4))
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"titleId": "%s", "type": "MOVIE", "score": 4, "available": true}
                        """.formatted(movie))
                .hasPathSatisfying("$.ratedAt", value -> assertThat(value).isNotNull());
        assertThat(rate(user, movie, 2)).bodyJson().extractingPath("$.score").isEqualTo(2);

        assertThat(ratings(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "score": 2}], "page": {"totalElements": 1}}
                """.formatted(movie));
    }

    @Test
    void shouldRateASeriesWithoutWatchingIt() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = newSeries(admin);

        assertThat(rate(user, series, 5))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("SERIES");
    }

    @Test
    void shouldNotRateEpisodes() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = newSeries(admin);
        catalog.addSeason(admin, series, 1);
        String episode = catalog.addEpisode(admin, series, 1, 1);

        assertThat(rate(user, episode, 5)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRejectScoresThatAreNotIntegersFromOneToFive() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = newMovie(admin);

        for (String body : new String[] {"{}", "{\"score\": 0}", "{\"score\": 6}", "{\"score\": 4.5}"}) {
            assertThat(catalog.send(user, "PUT", "/api/v1/me/ratings/" + movie, body))
                    .as(body)
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void shouldKeepRatingsOfRetiredTitlesWithoutChangingThemButAllowRemovingThem() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = newMovie(admin);
        rate(user, movie, 4);
        catalog.changeAvailability(admin, "/api/v1/movies/" + movie, "RETIRED");

        assertThat(rate(user, movie, 5))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:content-unavailable");
        assertThat(ratings(user)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "score": 4, "available": false}]}
                """.formatted(movie));
        assertThat(detail(user, "/api/v1/movies/" + movie))
                .bodyJson()
                .extractingPath("$.ratings.count")
                .isEqualTo(1);

        assertThat(remove(user, movie)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(ratings(user))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
    }

    @Test
    void shouldRemoveARatingThatDoesNotExistWithoutError() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();

        assertThat(remove(user, newMovie(admin))).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldShowTheAverageTheCountAndTheOwnScoreInTheTitleDetail() {
        Session admin = api.adminSession();
        Session first = api.newUserSession();
        Session second = api.newUserSession();
        Session third = api.newUserSession();
        String movie = newMovie(admin);
        rate(first, movie, 4);
        rate(second, movie, 5);
        rate(third, movie, 5);

        // 14 / 3 = 4.666…, shown with one decimal.
        assertThat(detail(first, "/api/v1/movies/" + movie)).bodyJson().isLenientlyEqualTo("""
                {"ratings": {"average": 4.7, "count": 3, "mine": 4}}
                """);
        assertThat(detail(api.newUserSession(), "/api/v1/movies/" + movie))
                .bodyJson()
                .extractingPath("$.ratings.mine")
                .isNull();
    }

    @Test
    void shouldShowEmptyRatingsInTheDetailOfATitleThatNobodyRated() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = newSeries(admin);

        assertThat(detail(user, "/api/v1/series/" + series)).bodyJson().isLenientlyEqualTo("""
                {"ratings": {"average": null, "count": 0, "mine": null}}
                """);
    }

    @Test
    void shouldShowRatingsOnlyInTheDetail() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String title = "Rated " + CatalogApi.token();
        String movie = catalog.createMovie(admin, title, 2000);
        rate(user, movie, 3);

        assertThat(detail(user, "/api/v1/movies/" + movie)).bodyJson().isLenientlyEqualTo("""
                        {"ratings": {"average": 3.0, "count": 1, "mine": 3}}
                        """);
        assertThat(catalog.get(user, "/api/v1/movies?q=" + title)).bodyJson().doesNotHavePath("$.content[0].ratings");
    }

    @Test
    void shouldCountRatingsOfDeactivatedAccounts() {
        Session admin = api.adminSession();
        Session leaving = api.newUserSession();
        Session staying = api.newUserSession();
        String movie = newMovie(admin);
        rate(leaving, movie, 1);
        rate(staying, movie, 5);

        assertThat(catalog.send(leaving, "POST", "/api/v1/me/deactivate", "{}")).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(detail(staying, "/api/v1/movies/" + movie)).bodyJson().isLenientlyEqualTo("""
                {"ratings": {"average": 3.0, "count": 2, "mine": 5}}
                """);
    }

    @Test
    void shouldKeepTheActivityOfAReactivatedAccount() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = newMovie(admin);
        rate(user, movie, 4);
        assertThat(catalog.send(user, "PUT", "/api/v1/me/progress/movies/" + movie, """
                        {"positionSeconds": 600}
                        """))
                .hasStatusOk();

        assertThat(catalog.send(user, "POST", "/api/v1/me/deactivate", "{}")).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(catalog.send(admin, "PATCH", "/api/v1/admin/users/" + user.userId(), """
                        {"status": "ACTIVE"}
                        """))
                .hasStatusOk();

        Session back = api.loginAs(user.email(), ApiClient.DEFAULT_PASSWORD);
        assertThat(ratings(back)).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "score": 4}]}
                """.formatted(movie));
        assertThat(catalog.get(back, "/api/v1/me/history")).bodyJson().isLenientlyEqualTo("""
                {"content": [{"titleId": "%s", "positionSeconds": 600}]}
                """.formatted(movie));
    }

    @Test
    void shouldNotDeleteTitlesWithRatings() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = newMovie(admin);
        String series = newSeries(admin);
        rate(user, movie, 3);
        rate(user, series, 3);

        assertThat(catalog.delete(admin, "/api/v1/movies/" + movie)).hasStatus(HttpStatus.CONFLICT);
        assertThat(catalog.delete(admin, "/api/v1/series/" + series)).hasStatus(HttpStatus.CONFLICT);
    }

    private String newMovie(Session admin) {
        return catalog.createMovie(admin, "Rated " + CatalogApi.token(), 2000);
    }

    private String newSeries(Session admin) {
        return catalog.createSeries(admin, "Rated " + CatalogApi.token(), 2000);
    }

    private MvcTestResult rate(Session user, String titleId, int score) {
        return catalog.send(user, "PUT", "/api/v1/me/ratings/" + titleId, """
                {"score": %d}
                """.formatted(score));
    }

    private MvcTestResult remove(Session user, String titleId) {
        return catalog.delete(user, "/api/v1/me/ratings/" + titleId);
    }

    private MvcTestResult ratings(Session user) {
        return catalog.get(user, "/api/v1/me/ratings");
    }

    private MvcTestResult detail(Session user, String uri) {
        return catalog.get(user, uri);
    }
}
