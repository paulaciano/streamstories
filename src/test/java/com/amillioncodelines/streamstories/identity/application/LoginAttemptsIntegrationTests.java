package com.amillioncodelines.streamstories.identity.application;

import static com.amillioncodelines.streamstories.support.ApiClient.DEFAULT_PASSWORD;
import static com.amillioncodelines.streamstories.support.ApiClient.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class LoginAttemptsIntegrationTests {

    private static final String WRONG_PASSWORD = "wrong-password";

    @Autowired
    ApiClient api;

    @Autowired
    LoginAttempts attempts;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void shouldBlockTheEmailAfterFiveFailuresEvenWithTheRightPassword() {
        String email = registered();
        failLogins(email, 5);

        var blocked = api.login(email, DEFAULT_PASSWORD);
        assertThat(blocked)
                .hasStatus(HttpStatus.TOO_MANY_REQUESTS)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:too-many-requests");
        assertThat(Long.parseLong(blocked.getResponse().getHeader(HttpHeaders.RETRY_AFTER)))
                .isBetween(1L, 900L);
    }

    @Test
    void shouldBlockEmailsWithoutAnAccountTheSameWay() {
        String email = uniqueEmail();
        failLogins(email, 5);

        assertThat(api.login(email, DEFAULT_PASSWORD)).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void shouldStartCountingAgainAfterASuccessfulLogin() {
        String email = registered();
        failLogins(email, 4);
        assertThat(api.login(email, DEFAULT_PASSWORD)).hasStatusOk();

        failLogins(email, 4);

        assertThat(api.login(email, DEFAULT_PASSWORD)).hasStatusOk();
    }

    @Test
    void shouldCountFailedReactivationRequests() {
        String email = registered();
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThat(api.requestReactivation(email, WRONG_PASSWORD)).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        assertThat(api.login(email, DEFAULT_PASSWORD)).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(api.requestReactivation(email, DEFAULT_PASSWORD)).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void shouldNotAffectOtherEmails() {
        String blocked = registered();
        String other = registered();
        failLogins(blocked, 5);

        assertThat(api.login(other, DEFAULT_PASSWORD)).hasStatusOk();
    }

    @Test
    void shouldLetTheEmailInAgainOnceTheWindowIsOverAndThenForgetIt() {
        String email = registered();
        failLogins(email, 5);
        jdbc.update("UPDATE login_attempt SET window_started_at = window_started_at - interval '16 minutes'");

        assertThat(attempts.deleteExpired()).isPositive();
        assertThat(api.login(email, DEFAULT_PASSWORD)).hasStatusOk();
    }

    private String registered() {
        String email = uniqueEmail();
        assertThat(api.register(email, DEFAULT_PASSWORD)).hasStatus(HttpStatus.CREATED);
        return email;
    }

    private void failLogins(String email, int times) {
        for (int attempt = 0; attempt < times; attempt++) {
            assertThat(api.login(email, WRONG_PASSWORD)).hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }
}
