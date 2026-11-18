package com.amillioncodelines.streamstories.activity;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@IntegrationTest
class ProgressIntegrationTests {

    // Durations used by CatalogApi: 6000 seconds for movies and 2700 for episodes.

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldSaveTheExactPositionWhenReportingMovieProgress() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);

        assertThat(reportMovie(user, movie, 1260))
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"movieId": "%s", "positionSeconds": 1260, "durationSeconds": 6000, "completed": false}
                        """.formatted(movie))
                .doesNotHavePath("$.episodeId")
                .hasPathSatisfying("$.lastWatchedAt", value -> assertThat(value).isNotNull());
    }

    @Test
    void shouldKeepContentCompletedWhenAShorterPositionIsReportedLater() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);

        assertThat(reportMovie(user, movie, 5400))
                .bodyJson()
                .extractingPath("$.completed")
                .isEqualTo(true);

        assertThat(reportMovie(user, movie, 300)).bodyJson().isLenientlyEqualTo("""
                        {"positionSeconds": 300, "completed": true}
                        """);
    }

    @Test
    void shouldCapThePositionWhenItExceedsTheDuration() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);

        assertThat(reportMovie(user, movie, 99_999)).bodyJson().isLenientlyEqualTo("""
                        {"positionSeconds": 6000, "completed": true}
                        """);
    }

    @Test
    void shouldReportEpisodeProgress() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String episode = newEpisode(admin);

        assertThat(reportEpisode(user, episode, 1260))
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"episodeId": "%s", "positionSeconds": 1260, "durationSeconds": 2700, "completed": false}
                        """.formatted(episode))
                .doesNotHavePath("$.movieId");
    }

    @Test
    void shouldKeepProgressSeparatePerUser() {
        Session admin = api.adminSession();
        Session first = api.newUserSession();
        Session second = api.newUserSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);

        reportMovie(first, movie, 5400);

        assertThat(reportMovie(second, movie, 60))
                .bodyJson()
                .extractingPath("$.completed")
                .isEqualTo(false);
    }

    @Test
    void shouldAcceptProgressFromAdministrators() {
        Session admin = api.adminSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);

        assertThat(reportMovie(admin, movie, 60)).hasStatusOk();
    }

    @Test
    void shouldNotFailWhenTheSameProgressIsReportedConcurrently() throws Exception {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);

        List<Callable<MvcTestResult>> reports = IntStream.range(0, 8)
                .<Callable<MvcTestResult>>mapToObj(i -> () -> reportMovie(user, movie, 100 + i))
                .toList();
        try (ExecutorService executor = Executors.newFixedThreadPool(reports.size())) {
            for (Future<MvcTestResult> report : executor.invokeAll(reports)) {
                assertThat(report.get()).hasStatusOk();
            }
        }
    }

    @Test
    void shouldRejectProgressWhenTheMovieIsRetired() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);
        catalog.changeAvailability(admin, "/api/v1/movies/" + movie, "RETIRED");

        assertThat(reportMovie(user, movie, 60))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:content-unavailable");
    }

    @Test
    void shouldRejectProgressWhenTheEpisodeSeasonIsRetiredAndKeepWhatWasSaved() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = catalog.createSeries(admin, "Progress " + CatalogApi.token(), 2000);
        catalog.addSeason(admin, series, 1);
        String episode = catalog.addEpisode(admin, series, 1, 1);
        reportEpisode(user, episode, 600);

        catalog.changeAvailability(admin, "/api/v1/series/%s/seasons/1".formatted(series), "RETIRED");

        assertThat(reportEpisode(user, episode, 900)).hasStatus(HttpStatus.CONFLICT);
        // The saved progress blocks deletion (rule C11), so it is still there.
        assertThat(catalog.delete(admin, "/api/v1/episodes/" + episode)).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturnNotFoundWhenTheContentDoesNotExist() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String series = catalog.createSeries(admin, "Progress " + CatalogApi.token(), 2000);

        // A series is not a movie: its episodes are what gets played.
        assertThat(reportMovie(user, series, 60)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(reportEpisode(user, series, 60)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRejectMissingOrNegativePositions() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);

        assertThat(catalog.send(user, "PUT", "/api/v1/me/progress/movies/" + movie, "{}"))
                .hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(reportMovie(user, movie, -1))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("positionSeconds");
    }

    @Test
    void shouldNotDeleteContentWithProgress() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);
        String series = catalog.createSeries(admin, "Progress " + CatalogApi.token(), 2000);
        catalog.addSeason(admin, series, 1);
        String episode = catalog.addEpisode(admin, series, 1, 1);
        reportMovie(user, movie, 60);
        reportEpisode(user, episode, 60);

        assertThat(catalog.delete(admin, "/api/v1/movies/" + movie)).hasStatus(HttpStatus.CONFLICT);
        assertThat(catalog.delete(admin, "/api/v1/episodes/" + episode)).hasStatus(HttpStatus.CONFLICT);
        assertThat(catalog.delete(admin, "/api/v1/series/" + series)).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldDeleteContentWithoutProgress() {
        Session admin = api.adminSession();
        String movie = catalog.createMovie(admin, "Progress " + CatalogApi.token(), 2000);

        assertThat(catalog.delete(admin, "/api/v1/movies/" + movie)).hasStatus(HttpStatus.NO_CONTENT);
    }

    private String newEpisode(Session admin) {
        String series = catalog.createSeries(admin, "Progress " + CatalogApi.token(), 2000);
        catalog.addSeason(admin, series, 1);
        return catalog.addEpisode(admin, series, 1, 1);
    }

    private MvcTestResult reportMovie(Session session, String movieId, int positionSeconds) {
        return report(session, "/api/v1/me/progress/movies/" + movieId, positionSeconds);
    }

    private MvcTestResult reportEpisode(Session session, String episodeId, int positionSeconds) {
        return report(session, "/api/v1/me/progress/episodes/" + episodeId, positionSeconds);
    }

    private MvcTestResult report(Session session, String uri, int positionSeconds) {
        return catalog.send(session, "PUT", uri, """
                {"positionSeconds": %d}
                """.formatted(positionSeconds));
    }
}
