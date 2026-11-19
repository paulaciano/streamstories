package com.amillioncodelines.streamstories.shared.error;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(GlobalExceptionHandlerTests.ProbeController.class)
@AutoConfigureMockMvc(addFilters = false)
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
        assertThat(result).bodyJson().extractingPath("$.errors[1].message").isEqualTo("must not be blank");
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

    @Test
    void shouldMapAuthenticationFailuresTo401WithBearerChallenge() {
        assertThat(mvc.get().uri("/probe/unauthenticated"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:unauthorized");
    }

    @Test
    void shouldMapAccessDeniedTo403() {
        assertThat(mvc.get().uri("/probe/forbidden"))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:forbidden");
    }

    @Test
    void shouldMapOutOfRangeDatabaseValuesTo400() {
        assertThat(mvc.get().uri("/probe/out-of-range"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:validation-error");
    }

    @Test
    void shouldMapConcurrentModificationsTo409() {
        assertThat(mvc.get().uri("/probe/stale"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:conflict");
    }

    @Test
    void shouldMapDatabaseConstraintViolationsTo409WithoutLeakingDetails() {
        MvcTestResult result = mvc.get().uri("/probe/constraint").exchange();

        assertThat(result)
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:conflict");
        assertThat(result).bodyText().doesNotContain("uk_secret_constraint");
    }

    @Test
    void shouldReportInvalidParameterValuesAsValidationErrors() {
        assertThat(mvc.get().uri("/probe/items/not-a-number"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.detail")
                .isEqualTo("Parameter 'number' has an invalid value.");
    }

    @Test
    void shouldGiveAProblemTypeToUnsupportedMethods() {
        assertThat(mvc.delete().uri("/probe"))
                .hasStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"type": "urn:streamstories:problem:method-not-allowed", "title": "Method not allowed"}
                        """);
    }

    @Test
    void shouldGiveAProblemTypeToUnsupportedMediaTypes() {
        assertThat(mvc.post().uri("/probe").contentType(MediaType.TEXT_PLAIN).content("title"))
                .hasStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:unsupported-media-type");
    }

    @Test
    void shouldAnswerInEnglishWhateverLanguageTheClientAsksFor() {
        assertThat(mvc.post()
                        .uri("/probe")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "es-AR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "durationSeconds": 1}
                                """))
                .bodyJson()
                .isLenientlyEqualTo("""
                        {"title": "Invalid data", "errors": [{"field": "title", "message": "must not be blank"}]}
                        """);
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
                case "unauthenticated" -> throw new BadCredentialsException("bad token");
                case "forbidden" -> throw new AccessDeniedException("denied");
                case "constraint" -> throw new DataIntegrityViolationException("uk_secret_constraint");
                case "out-of-range" ->
                    throw new DataIntegrityViolationException(
                            "value too long", new SQLException("value too long", "22001"));
                case "stale" -> throw new ObjectOptimisticLockingFailureException(Object.class, id);
                default -> throw new ResourceNotFoundException("Movie %s not found".formatted(id));
            }
        }

        @GetMapping("/probe/items/{number}")
        int item(@PathVariable int number) {
            return number;
        }
    }
}
