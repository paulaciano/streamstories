package com.amillioncodelines.streamstories.catalog;

import static com.amillioncodelines.streamstories.support.CatalogApi.token;
import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@IntegrationTest
class FranchiseIntegrationTests {

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldKeepTheMoviesOfAFranchiseInTheGivenOrder() {
        Session admin = api.adminSession();
        String second = newMovie(admin);
        String first = newMovie(admin);
        String name = "Saga " + token();

        MvcTestResult created = create(admin, name, first, second);
        assertThat(created).hasStatus(HttpStatus.CREATED);
        String franchise = ApiClient.read(created, "$.id");

        assertThat(catalog.get(api.newUserSession(), "/api/v1/franchises/" + franchise))
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"name": "%s", "movies": [{"id": "%s", "order": 1}, {"id": "%s", "order": 2}]}
                        """.formatted(name, first, second));
        assertThat(catalog.get(admin, "/api/v1/franchises?size=100"))
                .bodyJson()
                .extractingPath("$.content[*].id")
                .asArray()
                .contains(franchise);
    }

    @Test
    void shouldReorderAndReleaseMoviesWhenTheFranchiseIsReplaced() {
        Session admin = api.adminSession();
        String a = newMovie(admin);
        String b = newMovie(admin);
        String c = newMovie(admin);
        String franchise = ApiClient.read(create(admin, "Saga " + token(), a, b, c), "$.id");

        assertThat(catalog.send(admin, "PUT", "/api/v1/franchises/" + franchise, body("Saga " + token(), c, a)))
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"movies": [{"id": "%s", "order": 1}, {"id": "%s", "order": 2}]}
                        """.formatted(c, a));
        assertThat(create(admin, "Saga " + token(), b)).hasStatus(HttpStatus.CREATED);
    }

    @Test
    void shouldNotPutAMovieInTwoFranchises() {
        Session admin = api.adminSession();
        String movie = newMovie(admin);
        create(admin, "Saga " + token(), movie);

        assertThat(create(admin, "Saga " + token(), movie)).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldRejectDuplicateNamesAndInvalidMovieLists() {
        Session admin = api.adminSession();
        String name = "Saga " + token();
        String movie = newMovie(admin);
        String series = catalog.createSeries(admin, "Not a movie " + token(), 2000);
        create(admin, name);

        assertThat(create(admin, name.toUpperCase())).hasStatus(HttpStatus.CONFLICT);
        assertThat(create(admin, "Saga " + token(), movie, movie)).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(create(admin, "Saga " + token(), series)).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(catalog.send(admin, "POST", "/api/v1/franchises", """
                        {"name": "Saga without list"}
                        """)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldHideRetiredMoviesFromViewersOnly() {
        Session admin = api.adminSession();
        String available = newMovie(admin);
        String retired = newMovie(admin);
        String franchise = ApiClient.read(create(admin, "Saga " + token(), available, retired), "$.id");
        catalog.changeAvailability(admin, "/api/v1/movies/" + retired, "RETIRED");

        assertThat(catalog.get(api.newUserSession(), "/api/v1/franchises/" + franchise))
                .bodyJson()
                .extractingPath("$.movies[*].id")
                .asArray()
                .containsExactly(available);
        assertThat(catalog.get(admin, "/api/v1/franchises/" + franchise))
                .bodyJson()
                .extractingPath("$.movies[*].id")
                .asArray()
                .containsExactly(available, retired);
    }

    @Test
    void shouldKeepTheMoviesWhenTheFranchiseIsDeleted() {
        Session admin = api.adminSession();
        String movie = newMovie(admin);
        String franchise = ApiClient.read(create(admin, "Saga " + token(), movie), "$.id");

        assertThat(catalog.delete(admin, "/api/v1/franchises/" + franchise)).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(catalog.get(admin, "/api/v1/franchises/" + franchise)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(catalog.get(admin, "/api/v1/movies/" + movie)).hasStatusOk();
        assertThat(create(admin, "Saga " + token(), movie)).hasStatus(HttpStatus.CREATED);
    }

    @Test
    void shouldLeaveOutADeletedMovie() {
        Session admin = api.adminSession();
        String kept = newMovie(admin);
        String deleted = newMovie(admin);
        String franchise = ApiClient.read(create(admin, "Saga " + token(), kept, deleted), "$.id");

        assertThat(catalog.delete(admin, "/api/v1/movies/" + deleted)).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(catalog.get(admin, "/api/v1/franchises/" + franchise))
                .bodyJson()
                .extractingPath("$.movies[*].id")
                .asArray()
                .containsExactly(kept);
    }

    private String newMovie(Session admin) {
        return catalog.createMovie(admin, "Saga movie " + token(), 2000);
    }

    private MvcTestResult create(Session admin, String name, String... movieIds) {
        return catalog.send(admin, "POST", "/api/v1/franchises", body(name, movieIds));
    }

    private static String body(String name, String... movieIds) {
        String ids = Arrays.stream(movieIds).map(id -> "\"" + id + "\"").collect(Collectors.joining(", "));
        return """
                {"name": "%s", "movieIds": [%s]}
                """.formatted(name, ids);
    }
}
