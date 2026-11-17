package com.amillioncodelines.streamstories.identity;

import static com.amillioncodelines.streamstories.support.ApiClient.DEFAULT_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Deactivation, reactivation requests and user administration (rules I3 to I7, ADR-0010). */
@IntegrationTest
class AccountLifecycleIntegrationTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ApiClient api;

    @Test
    void shouldBlockDeactivatedAccountImmediatelyEvenWithAValidAccessToken() {
        Session user = api.newUserSession();

        assertThat(deactivateOwnAccount(user)).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(mvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, user.bearer()))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:account-inactive");
        assertThat(api.refresh(user.refreshToken())).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReportInactiveAccountOnLoginOnlyWithCorrectCredentials() {
        Session user = api.newUserSession();
        deactivateOwnAccount(user);

        MvcTestResult login = api.login(user.email(), DEFAULT_PASSWORD);
        assertThat(login)
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:account-inactive");
        assertThat(login)
                .bodyJson()
                .extractingPath("$.reactivationRequestPending")
                .isEqualTo(false);

        assertThat(api.login(user.email(), "wrong-password")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReactivateAccountWhenAnAdminApprovesTheRequest() {
        Session user = api.newUserSession();
        deactivateOwnAccount(user);

        MvcTestResult first = api.requestReactivation(user.email(), DEFAULT_PASSWORD);
        assertThat(first).hasStatus(HttpStatus.ACCEPTED);
        assertThat(first).bodyJson().extractingPath("$.status").isEqualTo("PENDING");
        String requestId = ApiClient.read(first, "$.id");

        MvcTestResult second = api.requestReactivation(user.email(), DEFAULT_PASSWORD);
        assertThat(second).hasStatus(HttpStatus.ACCEPTED);
        assertThat(second).bodyJson().extractingPath("$.id").isEqualTo(requestId);
        assertThat(api.login(user.email(), DEFAULT_PASSWORD))
                .bodyJson()
                .extractingPath("$.reactivationRequestPending")
                .isEqualTo(true);

        Session admin = api.adminSession();
        assertThat(mvc.get()
                        .uri("/api/v1/admin/reactivation-requests?size=100")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[*].id")
                .asArray()
                .contains(requestId);

        MvcTestResult approval = resolve(admin, requestId, "approve");
        assertThat(approval).hasStatusOk();
        assertThat(approval).bodyJson().extractingPath("$.status").isEqualTo("APPROVED");
        assertThat(approval)
                .bodyJson()
                .extractingPath("$.resolvedBy")
                .isEqualTo(admin.userId().toString());

        assertThat(api.login(user.email(), DEFAULT_PASSWORD)).hasStatusOk();
        assertThat(resolve(admin, requestId, "approve")).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldKeepAccountInactiveWhenAnAdminRejectsTheRequest() {
        Session user = api.newUserSession();
        deactivateOwnAccount(user);
        String requestId = ApiClient.read(api.requestReactivation(user.email(), DEFAULT_PASSWORD), "$.id");

        assertThat(resolve(api.adminSession(), requestId, "reject"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.status")
                .isEqualTo("REJECTED");

        assertThat(api.login(user.email(), DEFAULT_PASSWORD)).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldRejectReactivationRequestsForActiveAccounts() {
        Session user = api.newUserSession();

        assertThat(api.requestReactivation(user.email(), DEFAULT_PASSWORD)).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldForbidAdministrationToUsers() {
        Session user = api.newUserSession();

        assertThat(mvc.get().uri("/api/v1/admin/users").header(HttpHeaders.AUTHORIZATION, user.bearer()))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:forbidden");
    }

    @Test
    void shouldApplyRoleChangesWithoutWaitingForANewToken() {
        Session user = api.newUserSession();
        Session admin = api.adminSession();

        assertThat(updateUser(admin, user, """
                        {"role": "ADMIN"}
                        """))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.role")
                .isEqualTo("ADMIN");
        assertThat(mvc.get().uri("/api/v1/admin/users").header(HttpHeaders.AUTHORIZATION, user.bearer()))
                .hasStatusOk();

        assertThat(updateUser(admin, user, """
                        {"role": "USER"}
                        """)).hasStatusOk();
        assertThat(mvc.get().uri("/api/v1/admin/users").header(HttpHeaders.AUTHORIZATION, user.bearer()))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldLetAnAdminDeactivateAndReactivateAnAccountDirectly() {
        Session user = api.newUserSession();
        Session admin = api.adminSession();

        assertThat(updateUser(admin, user, """
                        {"status": "INACTIVE"}
                        """)).hasStatusOk();
        assertThat(mvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, user.bearer()))
                .hasStatus(HttpStatus.FORBIDDEN);

        assertThat(updateUser(admin, user, """
                        {"status": "ACTIVE"}
                        """)).hasStatusOk();
        assertThat(mvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, user.bearer()))
                .hasStatusOk();
    }

    @Test
    void shouldRequireAtLeastOneFieldWhenUpdatingAUser() {
        Session user = api.newUserSession();

        assertThat(updateUser(api.adminSession(), user, "{}")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldFilterUsersAndRejectUnsupportedSortFields() {
        Session admin = api.adminSession();

        assertThat(mvc.get()
                        .uri("/api/v1/admin/users?role=ADMIN&status=ACTIVE")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[*].email")
                .asArray()
                .contains(ApiClient.ADMIN_EMAIL);

        assertThat(mvc.get()
                        .uri("/api/v1/admin/users?sort=passwordHash")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.type")
                .isEqualTo("urn:streamstories:problem:validation-error");
    }

    private MvcTestResult deactivateOwnAccount(Session user) {
        return mvc.post()
                .uri("/api/v1/me/deactivate")
                .header(HttpHeaders.AUTHORIZATION, user.bearer())
                .exchange();
    }

    private MvcTestResult resolve(Session admin, String requestId, String action) {
        return mvc.post()
                .uri("/api/v1/admin/reactivation-requests/{id}/{action}", requestId, action)
                .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                .exchange();
    }

    private MvcTestResult updateUser(Session admin, Session user, String json) {
        return mvc.patch()
                .uri("/api/v1/admin/users/{id}", user.userId())
                .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }
}
