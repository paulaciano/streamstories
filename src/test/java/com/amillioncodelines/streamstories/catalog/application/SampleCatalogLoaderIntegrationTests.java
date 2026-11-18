package com.amillioncodelines.streamstories.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.catalog.domain.MediaItemRepository;
import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.json.JsonMapper;

/**
 * Loads the real sample catalog through the application services: every sample title has to pass the same rules as
 * the API. The loader only runs in the {@code dev} profile, so the test builds it directly.
 */
@IntegrationTest
class SampleCatalogLoaderIntegrationTests {

    @Autowired
    MediaItemRepository titles;

    @Autowired
    GenreService genres;

    @Autowired
    MovieService movies;

    @Autowired
    SeriesService series;

    @Autowired
    JsonMapper json;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldLoadTheSampleCatalogFollowingTheBusinessRules() {
        var loader = new SampleCatalogLoader(titles, genres, movies, series, json, transactionManager);

        assertThat(loader.load()).isEqualTo(11);

        Session user = api.newUserSession();
        assertThat(search(user, "Estación Austral"))
                .bodyJson()
                .extractingPath("$.content[0].type")
                .isEqualTo("SERIES");
        assertThat(search(user, "El archivo perdido"))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);

        String vecinos = ApiClient.read(search(user, "Vecinos"), "$.content[0].id");
        assertThat(catalog.get(user, "/api/v1/series/" + vecinos))
                .bodyJson()
                .extractingPath("$.seasons[*].number")
                .asArray()
                .containsExactly(1, 2);
    }

    private MvcTestResult search(Session session, String text) {
        return catalog.get(session, "/api/v1/catalog?q=" + text);
    }
}
