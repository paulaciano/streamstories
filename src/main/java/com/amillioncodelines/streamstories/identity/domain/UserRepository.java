package com.amillioncodelines.streamstories.identity.domain;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String normalizedEmail);

    boolean existsByEmail(String normalizedEmail);

    boolean existsByRole(Role role);

    /**
     * Locks every active administrator, so concurrent deactivations or demotions cannot leave the system without one
     * (rule I9).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.role = com.amillioncodelines.streamstories.identity.domain.Role.ADMIN"
            + " and u.status = com.amillioncodelines.streamstories.identity.domain.AccountStatus.ACTIVE")
    List<User> lockActiveAdmins();

    /** Lightweight lookup used on every authenticated request. */
    @Query("select new com.amillioncodelines.streamstories.identity.domain.AccountSnapshot(u.id, u.role, u.status)"
            + " from User u where u.id = :id")
    Optional<AccountSnapshot> findSnapshotById(UUID id);
}
