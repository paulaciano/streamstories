package com.amillioncodelines.streamstories.identity.application;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amillioncodelines.streamstories.identity.domain.Role;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.identity.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

class InitialAdminProvisionerTests {

    private final UserRepository users = mock();

    @Test
    void shouldCreateTheAdministratorWhenThereIsNone() {
        provisioner("admin@example.com", "a-strong-password").run(null);

        verify(users).save(any(User.class));
    }

    @Test
    void shouldDoNothingWhenAnAdministratorAlreadyExists() {
        when(users.existsByRole(Role.ADMIN)).thenReturn(true);

        provisioner("admin@example.com", "a-strong-password").run(null);

        verify(users, never()).save(any(User.class));
    }

    @Test
    void shouldDoNothingWithoutConfiguredCredentials() {
        provisioner(null, null).run(null);

        verify(users, never()).save(any(User.class));
    }

    @Test
    void shouldNotTakeOverAnExistingUserWithTheConfiguredEmail() {
        when(users.existsByEmail("admin@example.com")).thenReturn(true);

        provisioner(" Admin@Example.com ", "a-strong-password").run(null);

        verify(users, never()).save(any(User.class));
    }

    @Test
    void shouldStopTheStartupWithAWeakPassword() {
        for (String weak : new String[] {"short", "change-me", "ñ".repeat(40)}) {
            assertThatIllegalStateException()
                    .as(weak)
                    .isThrownBy(() -> provisioner("admin@example.com", weak).run(null));
        }
        verify(users, never()).save(any(User.class));
    }

    @SuppressWarnings("deprecation")
    private InitialAdminProvisioner provisioner(String email, String password) {
        return new InitialAdminProvisioner(
                users, NoOpPasswordEncoder.getInstance(), new AdminProperties(email, password, "Administrator"));
    }
}
