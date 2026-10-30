package com.gqibindlala.booking.service;

import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Optional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppUserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String fullName;
    private final boolean resetExistingPasswordOnce;

    public AdminBootstrap(AppUserRepository userRepository, JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap-admin.email:}") String email,
                          @Value("${app.bootstrap-admin.password:}") String password,
                          @Value("${app.bootstrap-admin.full-name:Initial administrator}") String fullName,
                          @Value("${app.bootstrap-admin.reset-existing-password-once:false}") boolean resetExistingPasswordOnce) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = password;
        this.fullName = fullName;
        this.resetExistingPasswordOnce = resetExistingPasswordOnce;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank()) {
            log.warn("Initial administrator was not created because ADMIN_EMAIL is blank");
            return;
        }
        Optional<AppUser> existingAdmin = userRepository.findByLoginIdentifier(email);
        if (existingAdmin.isPresent()) {
            AppUser admin = existingAdmin.get();
            if (admin.getRole() != AppRole.ADMIN) {
                log.warn("Administrator bootstrap identifier {} belongs to a non-administrator account", email);
                return;
            }
            if (password.isBlank()) {
                log.warn("Cannot verify or reset the existing administrator password for {} because ADMIN_PASSWORD is not set",
                    email);
                return;
            }
            boolean configuredPasswordMatches = passwordMatches(admin);
            if (configuredPasswordMatches) {
                log.info("Configured ADMIN_PASSWORD matches the stored password for administrator {}", email);
                if (resetExistingPasswordOnce && claimOneTimePasswordReset()) {
                    log.info("One-time password reset setting consumed for {}; configured password already matches", email);
                }
                return;
            }
            log.warn("Configured ADMIN_PASSWORD does not match the stored password for administrator {}", email);
            if (!resetExistingPasswordOnce) {
                log.info("Administrator password reset is disabled; leaving account {} unchanged", email);
                return;
            }
            if (!claimOneTimePasswordReset()) {
                log.info("Administrator password reset was already consumed; leaving account {} unchanged", email);
                return;
            }
            admin.setPasswordHash(passwordEncoder.encode(password));
            userRepository.save(admin);
            log.info("Applied the one-time administrator password reset for {}", email);
            return;
        }
        if (password.isBlank()) {
            log.warn("Initial administrator {} was not created because ADMIN_PASSWORD is not set", email);
            return;
        }
        AppUser admin = new AppUser();
        admin.setFullName(fullName);
        admin.setLoginIdentifier(email);
        admin.setPhone("Not provided");
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(AppRole.ADMIN);
        userRepository.save(admin);
        if (resetExistingPasswordOnce) {
            claimOneTimePasswordReset();
        }
        log.info("Created initial administrator account {}", email);
    }

    private boolean passwordMatches(AppUser admin) {
        try {
            return passwordEncoder.matches(password, admin.getPasswordHash());
        } catch (IllegalArgumentException exception) {
            log.error("Stored password hash is invalid for administrator {}", email);
            return false;
        }
    }

    private boolean claimOneTimePasswordReset() {
        return jdbcTemplate.update("""
            INSERT INTO admin_password_reset_state (id, reset_at)
            VALUES (1, CURRENT_TIMESTAMP)
            ON CONFLICT (id) DO NOTHING
            """) == 1;
    }
}