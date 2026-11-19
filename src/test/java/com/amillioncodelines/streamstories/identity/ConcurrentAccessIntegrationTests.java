package com.amillioncodelines.streamstories.identity;

import static com.amillioncodelines.streamstories.support.ApiClient.DEFAULT_PASSWORD;
import static com.amillioncodelines.streamstories.support.ApiClient.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Simultaneous requests on the same email or account, and stale tokens sent to the public auth endpoints. */
@IntegrationTest
class ConcurrentAccessIntegrationTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Autowired
    JwtEncoder jwtEncoder;

    @Test
    void shouldCheckAtMostFivePasswordsWhenWrongLoginsArriveTogether() throws Exception {
        String email = uniqueEmail();
        api.register(email, DEFAULT_PASSWORD);

        List<Integer> statuses = runTogether(
                30, () -> api.login(email, "wrong-password").getResponse().getStatus());

        assertThat(statuses).filteredOn(status -> status == 401).hasSizeLessThanOrEqualTo(5);
        assertThat(statuses).filteredOn(status -> status == 429).hasSizeGreaterThanOrEqualTo(25);
    }

    @Test
    void shouldNeverLoseAChangeWhenTwoAdminsUpdateTheSameUser() throws Exception {
        Session admin = api.adminSession();
        for (int round = 0; round < 10; round++) {
            Session user = api.newUserSession();
            String uri = "/api/v1/admin/users/" + user.userId();
            assertThat(patch(admin, uri, "{\"role\": \"ADMIN\"}")).hasStatusOk();

            List<Integer> statuses = runTogether(List.of(
                    () -> patch(admin, uri, "{\"role\": \"USER\"}")
                            .getResponse()
                            .getStatus(),
                    () -> patch(admin, uri, "{\"status\": \"INACTIVE\"}")
                            .getResponse()
                            .getStatus()));

            String role = roleOf(admin, user);
            String status = statusOf(admin, user);
            if (statuses.get(0) == 200) {
                assertThat(role).as("demotion answered 200").isEqualTo("USER");
            }
            if (statuses.get(1) == 200) {
                assertThat(status).as("deactivation answered 200").isEqualTo("INACTIVE");
            }
            assertThat(statuses).allMatch(code -> code == 200 || code == 409);
        }
    }

    @Test
    void shouldIgnoreAnExpiredAccessTokenOnThePublicAuthEndpoints() {
        Session user = api.newUserSession();
        String expired = "Bearer " + token(user, Instant.now().minus(1, ChronoUnit.HOURS));

        assertThat(post("/api/v1/auth/refresh", expired, """
                        {"refreshToken": "%s"}
                        """.formatted(user.refreshToken())))
                .hasStatusOk();
        assertThat(post("/api/v1/auth/login", expired, """
                        {"email": "%s", "password": "%s"}
                        """.formatted(user.email(), DEFAULT_PASSWORD)))
                .hasStatusOk();
    }

    @Test
    void shouldLetADeactivatedAccountRequestReactivationWithItsOldTokenAttached() {
        Session user = api.newUserSession();
        assertThat(catalog.send(user, "POST", "/api/v1/me/deactivate", "{}")).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(post("/api/v1/auth/reactivation-requests", user.bearer(), """
                        {"email": "%s", "password": "%s"}
                        """.formatted(
                                user.email(), DEFAULT_PASSWORD)))
                .hasStatus(HttpStatus.ACCEPTED);
    }

    private String roleOf(Session admin, Session user) {
        return field(admin, user, "role");
    }

    private String statusOf(Session admin, Session user) {
        return field(admin, user, "status");
    }

    private String field(Session admin, Session user, String name) {
        MvcTestResult users = catalog.get(admin, "/api/v1/admin/users?size=100&sort=createdAt,desc");
        List<String> values = JsonPath.read(
                new String(users.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8),
                "$.content[?(@.id == '%s')].%s".formatted(user.userId(), name));
        return values.getFirst();
    }

    private String token(Session user, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("streamstories")
                .subject(user.userId().toString())
                .issuedAt(expiresAt.minus(15, ChronoUnit.MINUTES))
                .expiresAt(expiresAt)
                .claim("role", "USER")
                .build();
        return jwtEncoder
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private MvcTestResult post(String uri, String authorization, String json) {
        return mvc.post()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private MvcTestResult patch(Session admin, String uri, String json) {
        return catalog.send(admin, "PATCH", uri, json);
    }

    private static <T> List<T> runTogether(int times, Callable<T> task) throws Exception {
        List<Callable<T>> tasks = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            tasks.add(task);
        }
        return runTogether(tasks);
    }

    private static <T> List<T> runTogether(List<Callable<T>> tasks) throws Exception {
        List<T> results = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(tasks.size())) {
            for (Future<T> future : executor.invokeAll(tasks)) {
                results.add(future.get());
            }
        }
        return results;
    }
}
