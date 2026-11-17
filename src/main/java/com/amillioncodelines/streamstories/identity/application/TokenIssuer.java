package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.RefreshToken;
import com.amillioncodelines.streamstories.identity.domain.RefreshTokenRepository;
import com.amillioncodelines.streamstories.identity.domain.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * Issues signed access tokens and opaque refresh tokens.
 *
 * <p>The access token only carries the user id ({@code sub}) and role; no personal data. The refresh token is a random
 * 256-bit value: it is returned to the client once and only its SHA-256 hash is stored.
 */
@Component
class TokenIssuer {

    static final String ROLE_CLAIM = "role";
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokens;
    private final SecurityProperties properties;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    TokenIssuer(
            JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokens, SecurityProperties properties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokens = refreshTokens;
        this.properties = properties;
        this.clock = clock;
    }

    IssuedTokens issueFor(User user) {
        Instant now = clock.instant();
        var ttl = properties.jwt().accessTokenTtl();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim(ROLE_CLAIM, user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String accessToken =
                jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        String refreshToken = newRefreshTokenValue();
        refreshTokens.save(new RefreshToken(user, hash(refreshToken), now.plus(properties.refreshTokenTtl())));
        return new IssuedTokens(accessToken, refreshToken, ttl);
    }

    static String hash(String refreshToken) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256").digest(refreshToken.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String newRefreshTokenValue() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
