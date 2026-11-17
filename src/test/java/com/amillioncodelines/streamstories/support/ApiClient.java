package com.amillioncodelines.streamstories.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Shortcuts for the authentication flows that most integration tests need as a starting point. */
@TestComponent
public class ApiClient {

    public static final String ADMIN_EMAIL = "admin@streamstories.test";
    public static final String ADMIN_PASSWORD = "admin-test-password";
    public static final String DEFAULT_PASSWORD = "correct-horse-battery";

    private final MockMvcTester mvc;

    ApiClient(MockMvcTester mvc) {
        this.mvc = mvc;
    }

    /** A unique email, so tests sharing the database never collide. */
    public static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    public MvcTestResult register(String email, String password) {
        return post("/api/v1/auth/register", """
                {"email": "%s", "password": "%s", "displayName": "Test User"}
                """.formatted(email, password));
    }

    public MvcTestResult login(String email, String password) {
        return post("/api/v1/auth/login", """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password));
    }

    public MvcTestResult refresh(String refreshToken) {
        return post("/api/v1/auth/refresh", """
                {"refreshToken": "%s"}
                """.formatted(refreshToken));
    }

    public MvcTestResult requestReactivation(String email, String password) {
        return post("/api/v1/auth/reactivation-requests", """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password));
    }

    /** Registers a new user and logs in. */
    public Session newUserSession() {
        String email = uniqueEmail();
        assertThat(register(email, DEFAULT_PASSWORD)).hasStatus(HttpStatus.CREATED);
        return loginAs(email, DEFAULT_PASSWORD);
    }

    public Session adminSession() {
        return loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    public Session loginAs(String email, String password) {
        MvcTestResult result = login(email, password);
        assertThat(result).hasStatusOk();
        return new Session(email, read(result, "$.accessToken"), read(result, "$.refreshToken"), currentUserId(result));
    }

    public static String read(MvcTestResult result, String jsonPath) {
        return JsonPath.read(
                new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8), jsonPath);
    }

    private UUID currentUserId(MvcTestResult login) {
        MvcTestResult me = mvc.get()
                .uri("/api/v1/me")
                .header("Authorization", "Bearer " + read(login, "$.accessToken"))
                .exchange();
        return UUID.fromString(read(me, "$.id"));
    }

    private MvcTestResult post(String uri, String json) {
        return mvc.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    /** An authenticated user: email, tokens and id. */
    public record Session(String email, String accessToken, String refreshToken, UUID userId) {

        public String bearer() {
            return "Bearer " + accessToken;
        }
    }
}
