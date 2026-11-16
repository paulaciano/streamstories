package com.amillioncodelines.streamstories;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class StreamstoriesApplicationTests {

    @Autowired
    MockMvcTester mvc;

    @Test
    void shouldReportHealthyWhenDatabaseIsReachable() {
        assertThat(mvc.get().uri("/actuator/health"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.status")
                .isEqualTo("UP");
    }

    @Test
    void shouldServeOpenApiSpecification() {
        assertThat(mvc.get().uri("/v3/api-docs"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.info.title")
                .isEqualTo("StreamStories API");
    }

    @Test
    void shouldNotExposeSensitiveActuatorEndpoints() {
        assertThat(mvc.get().uri("/actuator/env")).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(mvc.get().uri("/actuator/beans")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldAnswerUnknownRoutesWithProblemDetails() {
        assertThat(mvc.get().uri("/api/v1/does-not-exist"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:not-found");
    }
}
