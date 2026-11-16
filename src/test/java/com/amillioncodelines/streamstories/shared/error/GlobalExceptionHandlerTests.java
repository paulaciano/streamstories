package com.amillioncodelines.streamstories.shared.error;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Exercises the global error handling through a probe controller that triggers each error type. */
@WebMvcTest(GlobalExceptionHandlerTests.ProbeController.class)
@Import(GlobalExceptionHandlerTests.ProbeController.class)
class GlobalExceptionHandlerTests {

    private static final String PROBLEM_JSON = "application/problem+json";

    @Autowired
    MockMvcTester mvc;

    @Test
    void shouldReturnValidationProblemWithFieldErrors() {
        MvcTestResult result = mvc.post()
                .uri("/probe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "", "durationSeconds": 0}
                        """)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.type").isEqualTo("urn:streamstories:problem:validation-error");
        assertThat(result)
                .bodyJson()
                .extractingPath("$.errors[*].field")
                .asArray()
                .containsExactly("durationSeconds", "title");
    }

    @Test
    void shouldRejectUnknownJsonFieldsAsMalformedRequest() {
        MvcTestResult result = mvc.post()
                .uri("/probe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Arrival", "durationSeconds": 6960, "tittle": "typo"}
                        """)
                .exchange();

        assertThat(result)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:malformed-request");
    }

    @Test
    void shouldRejectUnreadableJsonAsMalformedRequest() {
        assertThat(mvc.post()
                        .uri("/probe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:malformed-request");
    }

    @Test
    void shouldMapResourceNotFoundTo404() {
        MvcTestResult result = mvc.get().uri("/probe/missing").exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND).hasContentType(PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.type").isEqualTo("urn:streamstories:problem:not-found");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("Movie missing not found");
        assertThat(result).bodyJson().extractingPath("$.instance").isEqualTo("/probe/missing");
    }

    @Test
    void shouldMapConflictTo409() {
        assertThat(mvc.get().uri("/probe/conflict"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:conflict");
    }

    @Test
    void shouldHideInternalDetailsOnUnexpectedErrors() {
        MvcTestResult result = mvc.get().uri("/probe/boom").exchange();

        assertThat(result)
                .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:internal-error");
        assertThat(result).bodyText().doesNotContain("secret-internal-detail");
    }

    record ProbeRequest(@NotBlank String title, @Positive int durationSeconds) {}

    @RestController
    static class ProbeController {

        @PostMapping("/probe")
        ProbeRequest create(@Valid @RequestBody ProbeRequest request) {
            return request;
        }

        @GetMapping("/probe/{id}")
        void find(@PathVariable String id) {
            switch (id) {
                case "conflict" -> throw new ConflictException("Already exists");
                case "boom" -> throw new IllegalStateException("secret-internal-detail");
                default -> throw new ResourceNotFoundException("Movie %s not found".formatted(id));
            }
        }
    }
}
