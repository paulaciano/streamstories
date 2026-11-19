package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.ReactivationRequest;
import com.amillioncodelines.streamstories.identity.domain.ReactivationRequestRepository;
import com.amillioncodelines.streamstories.identity.domain.ReactivationStatus;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.identity.domain.UserRepository;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import com.amillioncodelines.streamstories.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/** Reactivation requests: inactive users ask for their account back and an administrator decides (ADR-0010). */
@Service
public class ReactivationService {

    private final ReactivationRequestRepository requests;
    private final UserRepository users;
    private final CredentialsVerifier credentials;
    private final TransactionTemplate transaction;
    private final Clock clock;

    ReactivationService(
            ReactivationRequestRepository requests,
            UserRepository users,
            CredentialsVerifier credentials,
            PlatformTransactionManager transactionManager,
            Clock clock) {
        this.requests = requests;
        this.users = users;
        this.credentials = credentials;
        this.transaction = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    /**
     * Creates a pending request, or returns the one already pending (rule I6).
     *
     * <p>The password is checked before the transaction starts, so it does not hold a database connection. The user
     * row is then locked: two simultaneous requests get the same pending request instead of a conflict.
     */
    public ReactivationView request(String email, String password) {
        UUID userId = credentials.verify(email, password).getId();
        return transaction.execute(status -> {
            User user = users.lockById(userId).orElseThrow();
            if (user.isActive()) {
                throw new ConflictException("The account is already active.");
            }
            ReactivationRequest request = requests.findByUserIdAndStatus(userId, ReactivationStatus.PENDING)
                    .orElseGet(() -> requests.saveAndFlush(ReactivationRequest.pending(user, clock.instant())));
            return ReactivationView.of(request);
        });
    }

    @Transactional(readOnly = true)
    public Page<ReactivationView> list(ReactivationStatus status, Pageable pageable) {
        return requests.findByStatus(status, pageable).map(ReactivationView::of);
    }

    /** Approves a pending request and reactivates the account. */
    @Transactional
    public ReactivationView approve(UUID requestId, UUID adminId) {
        ReactivationRequest request = find(requestId);
        request.approve(adminId, clock.instant());
        request.getUser().activate();
        return ReactivationView.of(request);
    }

    /** Rejects a pending request. The account stays inactive. */
    @Transactional
    public ReactivationView reject(UUID requestId, UUID adminId) {
        ReactivationRequest request = find(requestId);
        request.reject(adminId, clock.instant());
        return ReactivationView.of(request);
    }

    private ReactivationRequest find(UUID requestId) {
        return requests.lockById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reactivation request %s does not exist.".formatted(requestId)));
    }
}
