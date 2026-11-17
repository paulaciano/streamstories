package com.amillioncodelines.streamstories.identity.application;

import com.amillioncodelines.streamstories.identity.domain.Role;
import com.amillioncodelines.streamstories.identity.domain.User;
import com.amillioncodelines.streamstories.identity.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first administrator on startup when none exists yet, using the configured credentials. Without
 * credentials, no administrator is created and a warning is logged: there are no default credentials.
 */
@Component
class InitialAdminProvisioner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminProvisioner.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties properties;

    InitialAdminProvisioner(UserRepository users, PasswordEncoder passwordEncoder, AdminProperties properties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.existsByRole(Role.ADMIN)) {
            return;
        }
        if (!properties.isConfigured()) {
            log.warn("No administrator exists and STREAMSTORIES_ADMIN_EMAIL / STREAMSTORIES_ADMIN_PASSWORD are not set;"
                    + " no administrator was created");
            return;
        }
        String email = User.normalizeEmail(properties.email());
        if (users.existsByEmail(email)) {
            log.warn("Cannot create the initial administrator: a user with the configured email already exists");
            return;
        }
        users.save(User.administrator(email, passwordEncoder.encode(properties.password()), properties.displayName()));
        log.info("Initial administrator created");
    }
}
