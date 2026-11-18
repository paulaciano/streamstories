package com.amillioncodelines.streamstories;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Authorization matrix of every protected endpoint: without a token the answer is 401, and an endpoint reserved to
 * administrators answers 403 to a {@code USER}. The authorized paths are covered by each feature's tests.
 */
@IntegrationTest
class AuthorizationIntegrationTests {

    private static final String ID = UUID.randomUUID().toString();

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ApiClient api;

    static Stream<Arguments> authenticatedEndpoints() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/api/v1/me"),
                Arguments.of(HttpMethod.POST, "/api/v1/me/deactivate"),
                Arguments.of(HttpMethod.POST, "/api/v1/auth/logout"),
                Arguments.of(HttpMethod.GET, "/api/v1/catalog"),
                Arguments.of(HttpMethod.GET, "/api/v1/genres"),
                Arguments.of(HttpMethod.GET, "/api/v1/genres/" + ID),
                Arguments.of(HttpMethod.GET, "/api/v1/movies"),
                Arguments.of(HttpMethod.GET, "/api/v1/movies/" + ID),
                Arguments.of(HttpMethod.GET, "/api/v1/series"),
                Arguments.of(HttpMethod.GET, "/api/v1/series/" + ID),
                Arguments.of(HttpMethod.GET, "/api/v1/series/" + ID + "/seasons/1"),
                Arguments.of(HttpMethod.GET, "/api/v1/episodes/" + ID),
                Arguments.of(HttpMethod.PUT, "/api/v1/me/progress/movies/" + ID),
                Arguments.of(HttpMethod.PUT, "/api/v1/me/progress/episodes/" + ID));
    }

    static Stream<Arguments> adminEndpoints() {
        return Stream.of(
                Arguments.of(HttpMethod.GET, "/api/v1/admin/users"),
                Arguments.of(HttpMethod.PATCH, "/api/v1/admin/users/" + ID),
                Arguments.of(HttpMethod.GET, "/api/v1/admin/reactivation-requests"),
                Arguments.of(HttpMethod.POST, "/api/v1/admin/reactivation-requests/" + ID + "/approve"),
                Arguments.of(HttpMethod.POST, "/api/v1/admin/reactivation-requests/" + ID + "/reject"),
                Arguments.of(HttpMethod.POST, "/api/v1/genres"),
                Arguments.of(HttpMethod.PUT, "/api/v1/genres/" + ID),
                Arguments.of(HttpMethod.DELETE, "/api/v1/genres/" + ID),
                Arguments.of(HttpMethod.POST, "/api/v1/movies"),
                Arguments.of(HttpMethod.PUT, "/api/v1/movies/" + ID),
                Arguments.of(HttpMethod.PATCH, "/api/v1/movies/" + ID),
                Arguments.of(HttpMethod.DELETE, "/api/v1/movies/" + ID),
                Arguments.of(HttpMethod.POST, "/api/v1/series"),
                Arguments.of(HttpMethod.PUT, "/api/v1/series/" + ID),
                Arguments.of(HttpMethod.PATCH, "/api/v1/series/" + ID),
                Arguments.of(HttpMethod.DELETE, "/api/v1/series/" + ID),
                Arguments.of(HttpMethod.POST, "/api/v1/series/" + ID + "/seasons"),
                Arguments.of(HttpMethod.PUT, "/api/v1/series/" + ID + "/seasons/1"),
                Arguments.of(HttpMethod.PATCH, "/api/v1/series/" + ID + "/seasons/1"),
                Arguments.of(HttpMethod.DELETE, "/api/v1/series/" + ID + "/seasons/1"),
                Arguments.of(HttpMethod.POST, "/api/v1/series/" + ID + "/seasons/1/episodes"),
                Arguments.of(HttpMethod.PUT, "/api/v1/episodes/" + ID),
                Arguments.of(HttpMethod.DELETE, "/api/v1/episodes/" + ID));
    }

    static Stream<Arguments> protectedEndpoints() {
        return Stream.concat(authenticatedEndpoints(), adminEndpoints());
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("protectedEndpoints")
    void shouldRequireATokenOnProtectedEndpoints(HttpMethod method, String uri) {
        assertThat(mvc.method(method)
                        .uri(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:unauthorized");
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("adminEndpoints")
    void shouldForbidAdministrationEndpointsToUsers(HttpMethod method, String uri) {
        var user = api.newUserSession();

        assertThat(mvc.method(method)
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, user.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:forbidden");
    }
}
