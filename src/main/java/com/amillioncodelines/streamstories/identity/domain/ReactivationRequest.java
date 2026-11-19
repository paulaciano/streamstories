package com.amillioncodelines.streamstories.identity.domain;

import com.amillioncodelines.streamstories.shared.error.ConflictException;
import com.amillioncodelines.streamstories.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.util.UUID;

/**
 * A request from an inactive user to get their account back. Only an administrator can resolve it, and each
 * resolution records who took it and when.
 */
@Entity
public class ReactivationRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactivationStatus status;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt;

    private Instant resolvedAt;

    private UUID resolvedBy;

    protected ReactivationRequest() {}

    private ReactivationRequest(User user, Instant requestedAt) {
        this.user = user;
        this.status = ReactivationStatus.PENDING;
        this.requestedAt = requestedAt;
    }

    public static ReactivationRequest pending(User user, Instant now) {
        return new ReactivationRequest(user, now);
    }

    public void approve(UUID adminId, Instant now) {
        resolve(ReactivationStatus.APPROVED, adminId, now);
    }

    public void reject(UUID adminId, Instant now) {
        resolve(ReactivationStatus.REJECTED, adminId, now);
    }

    private void resolve(ReactivationStatus resolution, UUID adminId, Instant now) {
        if (status != ReactivationStatus.PENDING) {
            throw new ConflictException("The request was already resolved (%s).".formatted(status));
        }
        status = resolution;
        resolvedBy = adminId;
        resolvedAt = now;
    }

    public User getUser() {
        return user;
    }

    public ReactivationStatus getStatus() {
        return status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public UUID getResolvedBy() {
        return resolvedBy;
    }
}
