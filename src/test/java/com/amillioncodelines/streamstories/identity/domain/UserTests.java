package com.amillioncodelines.streamstories.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTests {

    @Test
    void shouldRegisterActiveUserWithNormalizedEmail() {
        User user = User.register("  Viewer@Example.COM ", "{noop}hash", " Viewer ");

        assertThat(user.getEmail()).isEqualTo("viewer@example.com");
        assertThat(user.getDisplayName()).isEqualTo("Viewer");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void shouldOnlyCountActiveAdminsAsActiveAdmins() {
        User admin = User.administrator("admin@example.com", "{noop}hash", "Admin");
        assertThat(admin.isActiveAdmin()).isTrue();

        admin.deactivate();

        assertThat(admin.isActiveAdmin()).isFalse();
    }
}
