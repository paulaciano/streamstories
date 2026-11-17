package com.amillioncodelines.streamstories.catalog;

import static com.amillioncodelines.streamstories.support.CatalogApi.token;
import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Rules C3 and C8. */
@IntegrationTest
class GenreIntegrationTests {

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldCreateGenreWithSlugDerivedFromItsName() {
        String suffix = token();

        MvcTestResult result = catalog.send(api.adminSession(), "POST", "/api/v1/genres", """
                {"name": "Ciencia Ficción %s"}
                """.formatted(suffix));

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.slug").isEqualTo("ciencia-ficcion-" + suffix);
        assertThat(result.getResponse().getHeader(HttpHeaders.LOCATION))
                .isEqualTo("/api/v1/genres/" + ApiClient.read(result, "$.id"));
    }

    @Test
    void shouldRejectDuplicateGenreNamesIgnoringCase() {
        Session admin = api.adminSession();
        String name = "Noir " + token();
        catalog.createGenre(admin, name);

        assertThat(catalog.send(admin, "POST", "/api/v1/genres", """
                        {"name": "%s"}
                        """.formatted(name.toUpperCase())))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldNotDeleteAGenreThatClassifiesTitles() {
        Session admin = api.adminSession();
        String genreId = catalog.createGenre(admin, "Western " + token());
        catalog.createMovie(admin, "Movie " + token(), 1968, genreId);

        assertThat(catalog.delete(admin, "/api/v1/genres/" + genreId)).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldDeleteAnUnusedGenre() {
        Session admin = api.adminSession();
        String genreId = catalog.createGenre(admin, "Unused " + token());

        assertThat(catalog.delete(admin, "/api/v1/genres/" + genreId)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(catalog.get(admin, "/api/v1/genres/" + genreId)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldLetUsersListGenresButNotManageThem() {
        Session user = api.newUserSession();

        assertThat(catalog.get(user, "/api/v1/genres")).hasStatusOk();
        assertThat(catalog.send(user, "POST", "/api/v1/genres", """
                        {"name": "Forbidden %s"}
                        """.formatted(token())))
                .hasStatus(HttpStatus.FORBIDDEN);
    }
}
