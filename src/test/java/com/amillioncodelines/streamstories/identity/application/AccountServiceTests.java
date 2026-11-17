package com.amillioncodelines.streamstories.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amillioncodelines.streamstories.identity.domain.AccountStatus;
import com.amillioncodelines.streamstories.identity.domain.ReactivationRequestRepository;
import com.amillioncodelines.streamstories.identity.domain.RefreshTokenRepository;
import com.amillioncodelines.streamstories.identity.domain.RevocationReason;
import com.amillioncodelines.streamstories.identity.domain.Role;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.identity.domain.UserRepository;
import com.amillioncodelines.streamstories.shared.error.ConflictException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Rule I9: the system can never be left without an active administrator. */
class AccountServiceTests {

    private final UserRepository users = mock();
    private final RefreshTokenRepository refreshTokens = mock();
    private final ReactivationRequestRepository reactivationRequests = mock();
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC);
    private final AccountService service = new AccountService(users, refreshTokens, reactivationRequests, clock);

    @Test
    void shouldNotLetTheLastActiveAdminDeactivateTheirAccount() {
        User admin = persisted(User.administrator("admin@example.com", "{noop}x", "Admin"));
        when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(users.lockActiveAdmins()).thenReturn(List.of(admin));

        assertThatThrownBy(() -> service.deactivateOwnAccount(admin.getId())).isInstanceOf(ConflictException.class);
        assertThat(admin.isActive()).isTrue();
        verify(refreshTokens, never()).revokeAllForUser(any(), any(), any());
    }

    @Test
    void shouldNotDemoteTheLastActiveAdmin() {
        User admin = persisted(User.administrator("admin@example.com", "{noop}x", "Admin"));
        when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(users.lockActiveAdmins()).thenReturn(List.of(admin));

        assertThatThrownBy(() -> service.updateUser(admin.getId(), null, Role.USER, admin.getId()))
                .isInstanceOf(ConflictException.class);
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldDeactivateAnAdminWhenAnotherActiveAdminRemains() {
        User admin = persisted(User.administrator("admin@example.com", "{noop}x", "Admin"));
        User other = persisted(User.administrator("other@example.com", "{noop}x", "Other"));
        when(users.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(users.lockActiveAdmins()).thenReturn(List.of(admin, other));
        when(users.saveAndFlush(admin)).thenReturn(admin);

        UserView result = service.updateUser(admin.getId(), AccountStatus.INACTIVE, null, other.getId());

        assertThat(result.status()).isEqualTo(AccountStatus.INACTIVE);
        verify(refreshTokens).revokeAllForUser(admin.getId(), clock.instant(), RevocationReason.ACCOUNT_DEACTIVATED);
    }

    @Test
    void shouldRevokeEveryRefreshTokenWhenDeactivating() {
        User viewer = persisted(User.register("viewer@example.com", "{noop}x", "Viewer"));
        when(users.findById(viewer.getId())).thenReturn(Optional.of(viewer));

        service.deactivateOwnAccount(viewer.getId());

        assertThat(viewer.isActive()).isFalse();
        verify(refreshTokens).revokeAllForUser(viewer.getId(), clock.instant(), RevocationReason.ACCOUNT_DEACTIVATED);
        verify(users, never()).lockActiveAdmins();
    }

    /** Simulates a persisted entity by assigning the identifier the database would generate. */
    private static User persisted(User user) {
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
