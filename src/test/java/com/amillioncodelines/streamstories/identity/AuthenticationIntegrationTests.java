package com.amillioncodelines.streamstories.identity;

import static com.amillioncodelines.streamstories.support.ApiClient.DEFAULT_PASSWORD;
import static com.amillioncodelines.streamstories.support.ApiClient.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@IntegrationTest
class AuthenticationIntegrationTests {

    private static final String INVALID_CREDENTIALS = "Invalid credentials.";

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ApiClient api;

    @Test
    void shouldRegisterUserAndReturnTheirDataWithoutPassword() {
        String email = uniqueEmail();

        MvcTestResult result = api.register(email.toUpperCase(), DEFAULT_PASSWORD);

        assertThat(result).hasStatus(HttpStatus.CREATED).hasHeader(HttpHeaders.LOCATION, "/api/v1/me");
        assertThat(result).bodyJson().extractingPath("$.email").isEqualTo(email);
        assertThat(result).bodyJson().extractingPath("$.role").isEqualTo("USER");
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("ACTIVE");
        assertThat(result).bodyText().doesNotContain("password");
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {
        String email = uniqueEmail();
        assertThat(api.register(email, DEFAULT_PASSWORD)).hasStatus(HttpStatus.CREATED);

        assertThat(api.register(email.toUpperCase(), DEFAULT_PASSWORD))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:conflict");
    }

    @Test
    void shouldRejectPasswordsShorterThanEightCharacters() {
        assertThat(api.register(uniqueEmail(), "short"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("password");
    }

    @Test
    void shouldLoginAndAccessCurrentUser() {
        var session = api.newUserSession();

        assertThat(session.accessToken()).isNotBlank();
        assertThat(mvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.email")
                .isEqualTo(session.email());
    }

    @Test
    void shouldNotRevealWhetherTheEmailExistsWhenLoginFails() {
        var session = api.newUserSession();

        MvcTestResult wrongPassword = api.login(session.email(), "wrong-password");
        MvcTestResult unknownEmail = api.login(uniqueEmail(), "wrong-password");

        for (MvcTestResult result : new MvcTestResult[] {wrongPassword, unknownEmail}) {
            assertThat(result)
                    .hasStatus(HttpStatus.UNAUTHORIZED)
                    .bodyJson()
                    .extractingPath("$.detail")
                    .isEqualTo(INVALID_CREDENTIALS);
        }
    }

    @Test
    void shouldRejectRequestsWithoutValidToken() {
        assertThat(mvc.get().uri("/api/v1/me"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:unauthorized");

        assertThat(mvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:unauthorized");
    }

    @Test
    void shouldRotateRefreshTokens() {
        var session = api.newUserSession();

        MvcTestResult refreshed = api.refresh(session.refreshToken());

        assertThat(refreshed).hasStatusOk();
        String newRefreshToken = ApiClient.read(refreshed, "$.refreshToken");
        assertThat(newRefreshToken).isNotEqualTo(session.refreshToken());
        assertThat(api.refresh(newRefreshToken)).hasStatusOk();
    }

    @Test
    void shouldRevokeEveryRefreshTokenWhenARevokedOneIsReused() {
        var session = api.newUserSession();
        String rotated = ApiClient.read(api.refresh(session.refreshToken()), "$.refreshToken");

        assertThat(api.refresh(session.refreshToken())).hasStatus(HttpStatus.UNAUTHORIZED);

        assertThat(api.refresh(rotated)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() {
        var session = api.newUserSession();

        assertThat(mvc.post()
                        .uri("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken": "%s"}
                                """.formatted(session.refreshToken())))
                .hasStatus(HttpStatus.NO_CONTENT);

        assertThat(api.refresh(session.refreshToken())).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldNotEndOtherSessionsWhenALoggedOutTokenIsPresentedAgain() {
        var laptop = api.newUserSession();
        var phone = api.loginAs(laptop.email(), DEFAULT_PASSWORD);
        mvc.post()
                .uri("/api/v1/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, laptop.bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"refreshToken": "%s"}
                        """.formatted(laptop.refreshToken()))
                .exchange();

        assertThat(api.refresh(laptop.refreshToken())).hasStatus(HttpStatus.UNAUTHORIZED);

        assertThat(api.refresh(phone.refreshToken())).hasStatusOk();
    }

    @Test
    void shouldRejectUnknownRefreshTokens() {
        assertThat(api.refresh("unknown-token"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:unauthorized");
    }
}
