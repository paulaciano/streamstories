package com.amillioncodelines.streamstories.catalog;

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
class SeriesIntegrationTests {

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldShowSeriesWithSeasonSummariesAndOrderedEpisodes() {
        Session admin = api.adminSession();
        String id = catalog.createSeries(admin, "Dark " + token(), 2017);
        catalog.addSeason(admin, id, 2);
        catalog.addSeason(admin, id, 1);
        catalog.addEpisode(admin, id, 1, 2);
        catalog.addEpisode(admin, id, 1, 1);
        Session user = api.newUserSession();

        MvcTestResult detail = catalog.get(user, "/api/v1/series/" + id);
        assertThat(detail).hasStatusOk();
        assertThat(detail)
                .bodyJson()
                .extractingPath("$.seasons[*].number")
                .asArray()
                .containsExactly(1, 2);
        assertThat(detail)
                .bodyJson()
                .extractingPath("$.seasons[*].episodeCount")
                .asArray()
                .containsExactly(2, 0);

        assertThat(catalog.get(user, "/api/v1/series/" + id + "/seasons/1"))
                .bodyJson()
                .extractingPath("$.episodes[*].number")
                .asArray()
                .containsExactly(1, 2);
    }

    @Test
    void shouldAllowASeriesWithoutSeasons() {
        Session admin = api.adminSession();
        String id = catalog.createSeries(admin, "Coming soon " + token(), 2026);

        assertThat(catalog.get(admin, "/api/v1/series/" + id))
                .bodyJson()
                .extractingPath("$.seasons")
                .asArray()
                .isEmpty();
    }

    @Test
    void shouldEnforceUniqueNumbersStartingAtOne() {
        Session admin = api.adminSession();
        String id = catalog.createSeries(admin, "Numbers " + token(), 2020);
        catalog.addSeason(admin, id, 1);
        catalog.addEpisode(admin, id, 1, 1);

        assertThat(catalog.send(admin, "POST", "/api/v1/series/" + id + "/seasons", """
                        {"number": 1}
                        """))
                .hasStatus(HttpStatus.CONFLICT);
        assertThat(catalog.send(admin, "POST", "/api/v1/series/" + id + "/seasons", """
                        {"number": 0}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(catalog.send(
                        admin,
                        "POST",
                        "/api/v1/series/" + id + "/seasons/1/episodes",
                        CatalogApi.episodeJson(1, "Duplicate")))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldRejectNumbersAboveTheLimitInsteadOfTruncatingThem() {
        Session admin = api.adminSession();
        String id = catalog.createSeries(admin, "Huge " + token(), 2020);

        assertThat(catalog.send(admin, "POST", "/api/v1/series/" + id + "/seasons", """
                        {"number": 65537}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST);
        catalog.addSeason(admin, id, 1);
        assertThat(catalog.send(
                        admin,
                        "POST",
                        "/api/v1/series/" + id + "/seasons/1/episodes",
                        CatalogApi.episodeJson(1000, "Too far")))
                .hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(catalog.get(admin, "/api/v1/series/" + id))
                .bodyJson()
                .extractingPath("$.seasons[*].number")
                .asArray()
                .containsExactly(1);
    }

    @Test
    void shouldHideRetiredSeasonsFromViewersButKeepThemReachable() {
        Session admin = api.adminSession();
        String id = catalog.createSeries(admin, "Partial " + token(), 2020);
        catalog.addSeason(admin, id, 1);
        catalog.addSeason(admin, id, 2);
        catalog.addEpisode(admin, id, 1, 1);
        String retired = catalog.addEpisode(admin, id, 2, 1);
        Session user = api.newUserSession();

        assertThat(catalog.changeAvailability(admin, "/api/v1/series/" + id + "/seasons/2", "RETIRED"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.availability")
                .isEqualTo("RETIRED");

        assertThat(catalog.get(user, "/api/v1/series/" + id))
                .bodyJson()
                .extractingPath("$.seasons[*].number")
                .asArray()
                .containsExactly(1);
        assertThat(catalog.get(admin, "/api/v1/series/" + id))
                .bodyJson()
                .extractingPath("$.seasons[*].availability")
                .asArray()
                .containsExactly("AVAILABLE", "RETIRED");

        MvcTestResult season = catalog.get(user, "/api/v1/series/" + id + "/seasons/2");
        assertThat(season).hasStatusOk();
        assertThat(season).bodyJson().extractingPath("$.availability").isEqualTo("RETIRED");

        MvcTestResult episode = catalog.get(user, "/api/v1/episodes/" + retired);
        assertThat(episode).hasStatusOk();
        assertThat(episode).bodyJson().extractingPath("$.playable").isEqualTo(false);
        assertThat(episode).bodyJson().extractingPath("$.videoUrl").isNull();
    }

    @Test
    void shouldOfferARetiredSeasonAgain() {
        Session admin = api.adminSession();
        String id = catalog.createSeries(admin, "Back " + token(), 2020);
        catalog.addSeason(admin, id, 1);
        String episodeId = catalog.addEpisode(admin, id, 1, 1);
        catalog.changeAvailability(admin, "/api/v1/series/" + id + "/seasons/1", "RETIRED");

        catalog.changeAvailability(admin, "/api/v1/series/" + id + "/seasons/1", "AVAILABLE");

        assertThat(catalog.get(api.newUserSession(), "/api/v1/episodes/" + episodeId))
                .bodyJson()
                .extractingPath("$.playable")
                .isEqualTo(true);
    }

    @Test
    void shouldNotRetireSingleEpisodes() {
        Session admin = api.adminSession();
        String id = catalog.createSeries(admin, "Whole seasons " + token(), 2020);
        catalog.addSeason(admin, id, 1);
        String episodeId = catalog.addEpisode(admin, id, 1, 1);

        assertThat(catalog.changeAvailability(admin, "/api/v1/episodes/" + episodeId, "RETIRED"))
                .hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void shouldMakeEveryEpisodeUnplayableWhenTheSeriesIsRetired() {
        Session admin = api.adminSession();
        String title = "Cancelled " + token();
        String id = catalog.createSeries(admin, title, 2020);
        catalog.addSeason(admin, id, 1);
        String episodeId = catalog.addEpisode(admin, id, 1, 1);
        Session user = api.newUserSession();

        catalog.changeAvailability(admin, "/api/v1/series/" + id, "RETIRED");

        assertThat(catalog.get(user, "/api/v1/series?q=" + title))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
        assertThat(catalog.get(user, "/api/v1/series/" + id)).hasStatusOk();
        MvcTestResult episode = catalog.get(user, "/api/v1/episodes/" + episodeId);
        assertThat(episode).bodyJson().extractingPath("$.playable").isEqualTo(false);
    }

    @Test
    void shouldReplaceAnEpisodeWithoutTakingAnotherEpisodesNumber() {
        Session admin = api.adminSession();
        String id = catalog.createSeries(admin, "Renumbered " + token(), 2020);
        catalog.addSeason(admin, id, 1);
        catalog.addEpisode(admin, id, 1, 1);
        String second = catalog.addEpisode(admin, id, 1, 2);

        assertThat(catalog.send(admin, "PUT", "/api/v1/episodes/" + second, CatalogApi.episodeJson(1, "Clash")))
                .hasStatus(HttpStatus.CONFLICT);
        assertThat(catalog.send(admin, "PUT", "/api/v1/episodes/" + second, CatalogApi.episodeJson(3, "Third")))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.number")
                .isEqualTo(3);
    }

    @Test
    void shouldOnlyDeleteEmptySeasonsAndThenTheWholeSeries() {
        Session admin = api.adminSession();
        String id = catalog.createSeries(admin, "Short lived " + token(), 2020);
        catalog.addSeason(admin, id, 1);
        String episodeId = catalog.addEpisode(admin, id, 1, 1);

        assertThat(catalog.delete(admin, "/api/v1/series/" + id + "/seasons/1")).hasStatus(HttpStatus.CONFLICT);
        assertThat(catalog.delete(admin, "/api/v1/episodes/" + episodeId)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(catalog.delete(admin, "/api/v1/series/" + id + "/seasons/1")).hasStatus(HttpStatus.NO_CONTENT);

        catalog.addSeason(admin, id, 1);
        catalog.addEpisode(admin, id, 1, 1);
        assertThat(catalog.delete(admin, "/api/v1/series/" + id)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(catalog.get(admin, "/api/v1/series/" + id)).hasStatus(HttpStatus.NOT_FOUND);
    }
}
