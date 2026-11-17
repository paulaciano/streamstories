package com.amillioncodelines.streamstories.identity.domain;

import com.amillioncodelines.streamstories.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Locale;
import java.util.Objects;

/**
 * A registered viewer. Since an account belongs to a single viewer, user and account are the same entity.
 *
 * <p>The table is named {@code app_user} because {@code user} is a reserved word in PostgreSQL.
 */
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

    /** Registration always creates an active {@link Role#USER} (rule I8). */
    public static User register(String email, String passwordHash, String displayName) {
        return new User(email, passwordHash, displayName, Role.USER);
    }

    /** Creates an active administrator. Only used to provision the initial admin. */
    public static User administrator(String email, String passwordHash, String displayName) {
        return new User(email, passwordHash, displayName, Role.ADMIN);
    }

    /** Emails are stored trimmed and in lower case, so lookups are case-insensitive (rule I1). */
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

    /** Whether this user currently counts towards the active administrators (rule I9). */
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
