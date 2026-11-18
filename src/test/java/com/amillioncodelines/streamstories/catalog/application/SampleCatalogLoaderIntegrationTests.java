package com.amillioncodelines.streamstories.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.catalog.domain.MediaItemRepository;
import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

        // Other tests share the database and may create titles with similar names: match the exact title.
        Session user = api.newUserSession();
        assertThat(search(user, "Estación Austral"))
                .bodyJson()
                .extractingPath("$.content[?(@.title == 'Estación Austral')].type")
                .asArray()
                .containsExactly("SERIES");
        assertThat(search(user, "El archivo perdido"))
                .bodyJson()
                .extractingPath("$.content[?(@.title == 'El archivo perdido')]")
                .asArray()
                .isEmpty();

        List<String> vecinos = JsonPath.read(
                new String(search(user, "Vecinos").getResponse().getContentAsByteArray(), StandardCharsets.UTF_8),
                "$.content[?(@.title == 'Vecinos' && @.type == 'SERIES')].id");
        assertThat(vecinos).hasSize(1);
        assertThat(catalog.get(user, "/api/v1/series/" + vecinos.getFirst()))
                .bodyJson()
                .extractingPath("$.seasons[*].number")
                .asArray()
                .containsExactly(1, 2);
    }

    private MvcTestResult search(Session session, String text) {
        return catalog.get(session, "/api/v1/catalog?size=100&q=" + text);
    }
}
