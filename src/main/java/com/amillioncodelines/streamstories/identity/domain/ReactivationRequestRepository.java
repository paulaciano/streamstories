package com.amillioncodelines.streamstories.identity.domain;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ReactivationRequestRepository extends JpaRepository<ReactivationRequest, UUID> {

    Optional<ReactivationRequest> findByUserIdAndStatus(UUID userId, ReactivationStatus status);

    boolean existsByUserIdAndStatus(UUID userId, ReactivationStatus status);

    /** Locks the request, so an approval and a rejection cannot both succeed. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReactivationRequest r join fetch r.user where r.id = :id")
    Optional<ReactivationRequest> lockById(UUID id);

    @EntityGraph(attributePaths = "user")
    Page<ReactivationRequest> findByStatus(ReactivationStatus status, Pageable pageable);
}
