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

@IntegrationTest
class CatalogEditingIntegrationTests {

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldListAvailableSeriesWithTheirGenres() {
        Session admin = api.adminSession();
        String genre = catalog.createGenre(admin, "Genre " + token());
        String title = "Listed " + token();
        String series = catalog.createSeries(admin, title, 2001, genre);

        assertThat(catalog.get(api.newUserSession(), "/api/v1/series?q=" + title))
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"content": [{"id": "%s", "title": "%s", "releaseYear": 2001, "availability": "AVAILABLE",
                                      "genres": [{"id": "%s"}]}],
                         "page": {"totalElements": 1}}
                        """.formatted(series, title, genre));
    }

    @Test
    void shouldReplaceASeries() {
        Session admin = api.adminSession();
        String series = catalog.createSeries(admin, "Before " + token(), 2001);
        String title = "After " + token();

        assertThat(catalog.send(admin, "PUT", "/api/v1/series/" + series, CatalogApi.seriesJson(title, 2005)))
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"title": "%s", "releaseYear": 2005, "genres": []}
                        """.formatted(title));
    }

    @Test
    void shouldRenameASeasonAndTreatABlankTitleAsNoTitle() {
        Session admin = api.adminSession();
        String series = catalog.createSeries(admin, "Seasons " + token(), 2001);
        catalog.addSeason(admin, series, 1);
        String season = "/api/v1/series/%s/seasons/1".formatted(series);

        assertThat(catalog.send(admin, "PUT", season, """
                        {"title": "  Origins  "}
                        """))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.title")
                .isEqualTo("Origins");
        assertThat(catalog.send(admin, "PUT", season, """
                        {"title": "   "}
                        """))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.title")
                .isNull();
        assertThat(catalog.send(admin, "POST", "/api/v1/series/%s/seasons".formatted(series), """
                        {"number": 2, "title": "   "}
                        """))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("$.title")
                .isNull();
    }

    @Test
    void shouldGetAndRenameAGenreKeepingNamesUnique() {
        Session admin = api.adminSession();
        String taken = "Taken " + token();
        catalog.createGenre(admin, taken);
        String genre = catalog.createGenre(admin, "Old " + token());
        String name = "New " + token();

        assertThat(catalog.send(admin, "PUT", "/api/v1/genres/" + genre, """
                        {"name": "%s"}
                        """.formatted(name)))
                .hasStatusOk();
        assertThat(catalog.get(api.newUserSession(), "/api/v1/genres/" + genre))
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"id": "%s", "name": "%s"}
                        """.formatted(genre, name));
        assertThat(catalog.send(admin, "PUT", "/api/v1/genres/" + genre, """
                        {"name": "%s"}
                        """.formatted(taken.toUpperCase())))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldRejectEmptyGenreIds() {
        Session admin = api.adminSession();

        assertThat(catalog.send(admin, "POST", "/api/v1/movies", """
                        {"title": "Null genre", "releaseYear": 2000, "durationSeconds": 60,
                         "videoUrl": "https://cdn.example.com/videos/null-genre.m3u8", "genreIds": [null]}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:validation-error");
        assertThat(catalog.send(admin, "POST", "/api/v1/series", """
                        {"title": "Null genre", "releaseYear": 2000, "genreIds": [null]}
                        """)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldRejectTextAndDecimalsInNumericFields() {
        Session admin = api.adminSession();

        for (String releaseYear : new String[] {"\"2000\"", "2000.5"}) {
            assertThat(catalog.send(admin, "POST", "/api/v1/series", """
                            {"title": "Numbers", "releaseYear": %s}
                            """.formatted(releaseYear)))
                    .as(releaseYear)
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }
    }
}
