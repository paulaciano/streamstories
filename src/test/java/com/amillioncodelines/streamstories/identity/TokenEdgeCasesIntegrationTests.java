package com.amillioncodelines.streamstories.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.amillioncodelines.streamstories.support.ApiClient;
import com.amillioncodelines.streamstories.support.ApiClient.Session;
import com.amillioncodelines.streamstories.support.CatalogApi;
import com.amillioncodelines.streamstories.support.IntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@IntegrationTest
class TokenEdgeCasesIntegrationTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ApiClient api;

    @Autowired
    CatalogApi catalog;

    @Autowired
    JwtEncoder jwtEncoder;

    @Test
    void shouldRejectAValidlySignedTokenOfAUserThatDoesNotExist() {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("streamstories")
                .subject(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .claim("role", "ADMIN")
                .build();
        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        assertThat(mvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldNotRefreshTheSessionOfAnInactiveAccount() {
        Session user = api.newUserSession();
        assertThat(catalog.send(user, "POST", "/api/v1/me/deactivate", "{}")).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(api.refresh(user.refreshToken())).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectPasswordsLongerThanSeventyTwoBytes() {
        // 40 characters, but 80 bytes in UTF-8: BCrypt would silently ignore everything after byte 72.
        String password = "ñ".repeat(40);

        assertThat(api.register(ApiClient.uniqueEmail(), password))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.detail")
                .isEqualTo("The password cannot exceed 72 bytes.");
    }
}
