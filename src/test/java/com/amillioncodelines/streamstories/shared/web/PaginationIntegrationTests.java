package com.amillioncodelines.streamstories.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@IntegrationTest
class PaginationIntegrationTests {

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/api/v1/catalog",
                "/api/v1/genres",
                "/api/v1/me/history",
                "/api/v1/me/ratings",
                "/api/v1/me/watchlist"
            })
    void shouldRejectPagesBeyondTheLargestOffset(String uri) {
        Session user = api.newUserSession();

        assertThat(catalog.get(user, uri + "?page=30000000&size=100"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:validation-error");
    }
}
