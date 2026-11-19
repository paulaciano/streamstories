package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.AccountStatus;
import com.amillioncodelines.streamstories.identity.domain.ReactivationRequestRepository;
import com.amillioncodelines.streamstories.identity.domain.ReactivationStatus;
import com.amillioncodelines.streamstories.identity.domain.RefreshTokenRepository;
import com.amillioncodelines.streamstories.identity.domain.RevocationReason;
import com.amillioncodelines.streamstories.identity.domain.Role;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.identity.domain.UserRepository;
import com.amillioncodelines.streamstories.identity.domain.UserSpecifications;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import com.amillioncodelines.streamstories.shared.error.ResourceNotFoundException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final ReactivationRequestRepository reactivationRequests;
    private final Clock clock;

    AccountService(
            UserRepository users,
            RefreshTokenRepository refreshTokens,
            ReactivationRequestRepository reactivationRequests,
            Clock clock) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.reactivationRequests = reactivationRequests;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public UserView currentUser(UUID userId) {
        return UserView.of(findUser(userId));
    }

    @Transactional
    public void deactivateOwnAccount(UUID userId) {
        deactivate(findUser(userId));
        users.flush();
    }

    @Transactional(readOnly = true)
    public Page<UserView> listUsers(AccountStatus status, Role role, Pageable pageable) {
        return users.findAll(UserSpecifications.filter(status, role), pageable).map(UserView::of);
    }

    @Transactional
    public UserView updateUser(UUID userId, AccountStatus newStatus, Role newRole, UUID adminId) {
        User user = findUser(userId);
        if (newRole != null && newRole != user.getRole()) {
            changeRole(user, newRole);
        }
        if (newStatus == AccountStatus.ACTIVE && !user.isActive()) {
            activate(user, adminId);
        } else if (newStatus == AccountStatus.INACTIVE && user.isActive()) {
            deactivate(user);
        }
        return UserView.of(users.saveAndFlush(user));
    }

    private void changeRole(User user, Role newRole) {
        if (newRole != Role.ADMIN && user.isActiveAdmin()) {
            ensureNotLastActiveAdmin(user);
        }
        user.changeRole(newRole);
    }

    private void activate(User user, UUID adminId) {
        user.activate();
        reactivationRequests
                .findByUserIdAndStatus(user.getId(), ReactivationStatus.PENDING)
                .ifPresent(request -> request.approve(adminId, clock.instant()));
    }

    private void deactivate(User user) {
        if (user.isActiveAdmin()) {
            ensureNotLastActiveAdmin(user);
        }
        user.deactivate();
        refreshTokens.revokeAllForUser(user.getId(), clock.instant(), RevocationReason.ACCOUNT_DEACTIVATED);
    }

    private void ensureNotLastActiveAdmin(User admin) {
        List<User> activeAdmins = users.lockActiveAdmins();
        boolean isTheOnlyOne = activeAdmins.size() == 1 && activeAdmins.contains(admin);
        if (isTheOnlyOne) {
            throw new ConflictException("The last active administrator cannot be deactivated or demoted.");
        }
    }

    private User findUser(UUID userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User %s does not exist.".formatted(userId)));
    }
}
