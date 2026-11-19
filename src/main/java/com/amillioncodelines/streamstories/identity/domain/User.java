package com.amillioncodelines.streamstories.identity.domain;

import com.amillioncodelines.streamstories.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Locale;
import java.util.Objects;

/** Mapped to {@code app_user} because {@code user} is a reserved word in PostgreSQL. */
@Entity
@Table(name = "app_user")
public class User extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    protected User() {}

    private User(String email, String passwordHash, String displayName, Role role) {
        this.email = normalizeEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.displayName = displayName.strip();
        this.role = role;
        this.status = AccountStatus.ACTIVE;
    }

    public static User register(String email, String passwordHash, String displayName) {
        return new User(email, passwordHash, displayName, Role.USER);
    }

    public static User administrator(String email, String passwordHash, String displayName) {
        return new User(email, passwordHash, displayName, Role.ADMIN);
    }

    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    public void deactivate() {
        status = AccountStatus.INACTIVE;
    }

    public void activate() {
        status = AccountStatus.ACTIVE;
    }

    public void changeRole(Role newRole) {
        role = Objects.requireNonNull(newRole);
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isActiveAdmin() {
        return role == Role.ADMIN && isActive();
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }
}
