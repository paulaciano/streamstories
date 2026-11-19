package com.amillioncodelines.streamstories.identity.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revokes a token only if it is still valid. Returns {@code 0} when another request revoked it first, which lets
     * the caller detect concurrent reuse.
     *
     * <p>The persistence context is not cleared, so entities loaded earlier in the transaction stay managed. Callers
     * must not rely on the in-memory state of the revoked token afterwards.
     */
    @Modifying(flushAutomatically = true)
    @Query("update RefreshToken t set t.revokedAt = :now, t.revocationReason = :reason"
            + " where t.id = :id and t.revokedAt is null")
    int revokeIfActive(UUID id, Instant now, RevocationReason reason);

    @Modifying(flushAutomatically = true)
    @Query("update RefreshToken t set t.revokedAt = :now, t.revocationReason = :reason"
            + " where t.user.id = :userId and t.revokedAt is null")
    int revokeAllForUser(UUID userId, Instant now, RevocationReason reason);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :now")
    int deleteExpiredBefore(Instant now);
}
