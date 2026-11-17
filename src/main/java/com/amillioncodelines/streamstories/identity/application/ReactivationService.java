package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.ReactivationRequest;
import com.amillioncodelines.streamstories.identity.domain.ReactivationRequestRepository;
import com.amillioncodelines.streamstories.identity.domain.ReactivationStatus;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import com.amillioncodelines.streamstories.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reactivation requests: inactive users ask for their account back and an administrator decides (ADR-0010). */
@Service
public class ReactivationService {

    private final ReactivationRequestRepository requests;
    private final CredentialsVerifier credentials;
    private final Clock clock;

    ReactivationService(ReactivationRequestRepository requests, CredentialsVerifier credentials, Clock clock) {
        this.requests = requests;
        this.credentials = credentials;
        this.clock = clock;
    }

    /** Creates a pending request, or returns the one already pending (rule I6). */
    @Transactional
    public ReactivationView request(String email, String password) {
        User user = credentials.verify(email, password);
        if (user.isActive()) {
            throw new ConflictException("La cuenta ya está activa.");
        }
        ReactivationRequest request = requests.findByUserIdAndStatus(user.getId(), ReactivationStatus.PENDING)
                .orElseGet(() -> requests.saveAndFlush(ReactivationRequest.pending(user, clock.instant())));
        return ReactivationView.of(request);
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
        return requests.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la solicitud de reactivación %s.".formatted(requestId)));
    }
}
