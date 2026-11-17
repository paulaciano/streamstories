package com.amillioncodelines.streamstories;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@IntegrationTest
class StreamstoriesApplicationTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ApiClient api;

    @Test
    void shouldReportHealthyWhenDatabaseIsReachable() {
        assertThat(mvc.get().uri("/actuator/health"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.status")
                .isEqualTo("UP");
    }

    @Test
    void shouldServeOpenApiSpecificationWithoutAuthentication() {
        assertThat(mvc.get().uri("/v3/api-docs"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.info.title")
                .isEqualTo("StreamStories API");
    }

    @Test
    void shouldNotExposeSensitiveActuatorEndpoints() {
        var admin = api.adminSession();
        assertThat(mvc.get().uri("/actuator/env").header("Authorization", admin.bearer()))
                .hasStatus(HttpStatus.NOT_FOUND);
        assertThat(mvc.get().uri("/actuator/beans").header("Authorization", admin.bearer()))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRequireAuthenticationForUnknownRoutes() {
        assertThat(mvc.get().uri("/api/v1/does-not-exist"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:unauthorized");
    }

    @Test
    void shouldAnswerUnknownRoutesWithProblemDetailsWhenAuthenticated() {
        var user = api.newUserSession();
        assertThat(mvc.get().uri("/api/v1/does-not-exist").header("Authorization", user.bearer()))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:not-found");
    }
}
