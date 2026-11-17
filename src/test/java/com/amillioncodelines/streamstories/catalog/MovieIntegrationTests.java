package com.amillioncodelines.streamstories.catalog;

import static com.amillioncodelines.streamstories.support.CatalogApi.token;
import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@IntegrationTest
class MovieIntegrationTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldCreateAMovieWithItsGenres() {
        Session admin = api.adminSession();
        String genreId = catalog.createGenre(admin, "Drama " + token());

        MvcTestResult result =
                catalog.send(admin, "POST", "/api/v1/movies", CatalogApi.movieJson("Arrival", 2016, genreId));

        assertThat(result).hasStatus(HttpStatus.CREATED);
        String id = ApiClient.read(result, "$.id");
        assertThat(result.getResponse().getHeader(HttpHeaders.LOCATION)).isEqualTo("/api/v1/movies/" + id);
        MvcTestResult detail = catalog.get(api.newUserSession(), "/api/v1/movies/" + id);
        assertThat(detail).hasStatusOk();
        assertThat(detail).bodyJson().extractingPath("$.genres[0].id").isEqualTo(genreId);
        assertThat(detail).bodyJson().extractingPath("$.availability").isEqualTo("AVAILABLE");
        assertThat(detail).bodyJson().extractingPath("$.videoUrl").asString().startsWith("https://");
    }

    @Test
    void shouldValidateMovieData() {
        MvcTestResult result = catalog.send(api.adminSession(), "POST", "/api/v1/movies", """
                {"title": " ", "releaseYear": 2016, "durationSeconds": 0, "videoUrl": "http://insecure.example.com/v"}
                """);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result)
                .bodyJson()
                .extractingPath("$.errors[*].field")
                .asArray()
                .containsExactly("durationSeconds", "title", "videoUrl");
    }

    @Test
    void shouldRejectUnknownGenres() {
        String unknown = UUID.randomUUID().toString();

        assertThat(catalog.send(
                        api.adminSession(), "POST", "/api/v1/movies", CatalogApi.movieJson("Movie", 2020, unknown)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.detail")
                .asString()
                .contains(unknown);
    }

    @Test
    void shouldReplaceMovieDataAndGenres() {
        Session admin = api.adminSession();
        String drama = catalog.createGenre(admin, "Drama " + token());
        String comedy = catalog.createGenre(admin, "Comedy " + token());
        String id = catalog.createMovie(admin, "Original title", 2000, drama);

        MvcTestResult result =
                catalog.send(admin, "PUT", "/api/v1/movies/" + id, CatalogApi.movieJson("New title", 2001, comedy));

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.title").isEqualTo("New title");
        assertThat(result).bodyJson().extractingPath("$.genres[*].id").asArray().containsExactly(comedy);
    }

    @Test
    void shouldUpdateTheModificationDateWhenOnlyTheGenresChange() {
        Session admin = api.adminSession();
        String drama = catalog.createGenre(admin, "Drama " + token());
        String comedy = catalog.createGenre(admin, "Comedy " + token());
        String json = CatalogApi.movieJson("Same data", 2000, drama);
        MvcTestResult created = catalog.send(admin, "POST", "/api/v1/movies", json);
        String id = ApiClient.read(created, "$.id");
        Instant createdAt = Instant.parse(ApiClient.read(created, "$.updatedAt"));

        MvcTestResult replaced = catalog.send(admin, "PUT", "/api/v1/movies/" + id, json.replace(drama, comedy));

        assertThat(replaced).hasStatusOk();
        assertThat(Instant.parse(ApiClient.read(replaced, "$.updatedAt"))).isAfter(createdAt);
    }

    @Test
    void shouldHideRetiredMoviesFromListingsButKeepThemReachableById() {
        Session admin = api.adminSession();
        Session user = api.newUserSession();
        String title = "Retired " + token();
        String id = catalog.createMovie(admin, title, 1999);

        assertThat(catalog.changeAvailability(admin, "/api/v1/movies/" + id, "RETIRED"))
                .hasStatusOk();

        assertThat(catalog.get(user, "/api/v1/movies?q=" + title))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
        assertThat(catalog.get(user, "/api/v1/catalog?q=" + title))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
        MvcTestResult detail = catalog.get(user, "/api/v1/movies/" + id);
        assertThat(detail).hasStatusOk();
        assertThat(detail).bodyJson().extractingPath("$.availability").isEqualTo("RETIRED");
        assertThat(detail).bodyJson().extractingPath("$.videoUrl").isNull();

        assertThat(catalog.get(admin, "/api/v1/movies?availability=RETIRED&q=" + title))
                .bodyJson()
                .extractingPath("$.content[*].id")
                .asArray()
                .containsExactly(id);
    }

    @Test
    void shouldOfferARetiredMovieAgain() {
        Session admin = api.adminSession();
        String title = "Comeback " + token();
        String id = catalog.createMovie(admin, title, 1999);
        catalog.changeAvailability(admin, "/api/v1/movies/" + id, "RETIRED");

        catalog.changeAvailability(admin, "/api/v1/movies/" + id, "AVAILABLE");

        assertThat(catalog.get(api.newUserSession(), "/api/v1/movies?q=" + title))
                .bodyJson()
                .extractingPath("$.content[*].id")
                .asArray()
                .containsExactly(id);
    }

    @Test
    void shouldDeleteAMovieWithoutActivity() {
        Session admin = api.adminSession();
        String id = catalog.createMovie(admin, "Loaded by mistake " + token(), 2020);

        assertThat(catalog.delete(admin, "/api/v1/movies/" + id)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(catalog.get(admin, "/api/v1/movies/" + id)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRequireAuthenticationToBrowseAndAdminToManage() {
        assertThat(mvc.get().uri("/api/v1/movies")).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(catalog.send(api.newUserSession(), "POST", "/api/v1/movies", CatalogApi.movieJson("Nope", 2020)))
                .hasStatus(HttpStatus.FORBIDDEN);
    }
}
