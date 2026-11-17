package com.amillioncodelines.streamstories.identity.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactivationRequestRepository extends JpaRepository<ReactivationRequest, UUID> {

    Optional<ReactivationRequest> findByUserIdAndStatus(UUID userId, ReactivationStatus status);

    boolean existsByUserIdAndStatus(UUID userId, ReactivationStatus status);

    @EntityGraph(attributePaths = "user")
    Page<ReactivationRequest> findByStatus(ReactivationStatus status, Pageable pageable);
}
