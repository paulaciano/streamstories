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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@IntegrationTest
class ConditionalUpdateIntegrationTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Test
    void shouldChangeAMovieOnlyIfTheClientReadItsCurrentVersion() {
        Session admin = api.adminSession();
        String title = "Versioned " + token();
        String movie = catalog.createMovie(admin, title, 2000);
        String uri = "/api/v1/movies/" + movie;
        String read = etag(catalog.get(admin, uri));

        MvcTestResult first = put(admin, uri, read, CatalogApi.movieJson(title, 2001));
        assertThat(first).hasStatusOk();
        assertThat(etag(first)).isNotEqualTo(read);

        // A second admin still holds the old version: their change is rejected instead of overwriting the first.
        assertThat(put(admin, uri, read, CatalogApi.movieJson(title, 2002)))
                .hasStatus(HttpStatus.PRECONDITION_FAILED)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:precondition-failed");
        assertThat(put(admin, uri, etag(first), CatalogApi.movieJson(title, 2002)))
                .hasStatusOk();
    }

    @Test
    void shouldKeepChangesWithoutIfMatchUnconditional() {
        Session admin = api.adminSession();
        String title = "Versioned " + token();
        String movie = catalog.createMovie(admin, title, 2000);

        assertThat(put(admin, "/api/v1/movies/" + movie, null, CatalogApi.movieJson(title, 2001)))
                .hasStatusOk();
    }

    @Test
    void shouldCheckTheVersionWhenRetiringASeries() {
        Session admin = api.adminSession();
        String series = catalog.createSeries(admin, "Versioned " + token(), 2000);
        String uri = "/api/v1/series/" + series;
        String read = etag(catalog.get(admin, uri));
        catalog.changeAvailability(admin, uri, "RETIRED");

        assertThat(mvc.patch()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .header(HttpHeaders.IF_MATCH, read)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"availability": "AVAILABLE"}
                                """))
                .hasStatus(HttpStatus.PRECONDITION_FAILED);
    }

    @Test
    void shouldRejectAnIfMatchThatIsNotAVersion() {
        Session admin = api.adminSession();
        String title = "Versioned " + token();
        String movie = catalog.createMovie(admin, title, 2000);

        assertThat(put(admin, "/api/v1/movies/" + movie, "\"abc\"", CatalogApi.movieJson(title, 2001)))
                .hasStatus(HttpStatus.PRECONDITION_FAILED);
    }

    private MvcTestResult put(Session admin, String uri, String ifMatch, String json) {
        var request = mvc.put()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        if (ifMatch != null) {
            request.header(HttpHeaders.IF_MATCH, ifMatch);
        }
        return request.exchange();
    }

    private static String etag(MvcTestResult result) {
        String etag = result.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).isNotBlank();
        return etag;
    }
}
