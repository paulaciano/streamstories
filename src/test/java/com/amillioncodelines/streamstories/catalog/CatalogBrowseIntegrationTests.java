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

/** The mixed catalog of movies and series. */
@IntegrationTest
class CatalogBrowseIntegrationTests {

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldListMoviesAndSeriesTogetherAndFilterThem() {
        Session admin = api.adminSession();
        String tag = token();
        String genreId = catalog.createGenre(admin, "Sci-Fi " + tag);
        String movie = catalog.createMovie(admin, "Movie " + tag, 2016, genreId);
        String series = catalog.createSeries(admin, "Series " + tag, 2019, genreId);
        catalog.createMovie(admin, "Other movie " + tag, 2010);
        Session user = api.newUserSession();

        assertThat(catalog.get(user, "/api/v1/catalog?q=" + tag + "&genre=sci-fi-" + tag + "&sort=releaseYear,desc"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[*].id")
                .asArray()
                .containsExactly(series, movie);

        assertThat(catalog.get(user, "/api/v1/catalog?q=" + tag + "&type=SERIES"))
                .bodyJson()
                .extractingPath("$.content[*].type")
                .asArray()
                .containsExactly("SERIES");

        assertThat(catalog.get(user, "/api/v1/catalog?q=" + tag.toUpperCase()))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(3);
    }

    @Test
    void shouldIgnoreTheAvailabilityFilterForViewers() {
        Session admin = api.adminSession();
        String tag = token();
        String retired = catalog.createMovie(admin, "Gone " + tag, 2000);
        catalog.changeAvailability(admin, "/api/v1/movies/" + retired, "RETIRED");

        assertThat(catalog.get(api.newUserSession(), "/api/v1/catalog?availability=RETIRED&q=" + tag))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
        assertThat(catalog.get(admin, "/api/v1/catalog?availability=RETIRED&q=" + tag))
                .bodyJson()
                .extractingPath("$.content[*].id")
                .asArray()
                .containsExactly(retired);
    }

    @Test
    void shouldTreatSearchWildcardsLiterally() {
        Session admin = api.adminSession();
        String tag = token();
        catalog.createMovie(admin, "Plain " + tag, 2000);

        assertThat(catalog.get(admin, "/api/v1/catalog?q=" + tag + "%25"))
                .bodyJson()
                .extractingPath("$.page.totalElements")
                .isEqualTo(0);
    }

    @Test
    void shouldRejectUnsupportedSortFieldsAndTypes() {
        Session user = api.newUserSession();

        assertThat(catalog.get(user, "/api/v1/catalog?sort=videoUrl")).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(catalog.get(user, "/api/v1/catalog?type=PODCAST")).hasStatus(HttpStatus.BAD_REQUEST);
    }
}
