package com.gqibindlala.booking.service;

import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String fullName;

    public AdminBootstrap(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap-admin.email:}") String email,
                          @Value("${app.bootstrap-admin.password:}") String password,
                          @Value("${app.bootstrap-admin.full-name:Initial administrator}") String fullName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank()) {
            log.warn("Initial administrator was not created because ADMIN_EMAIL is blank");
            return;
        }
        if (password.isBlank()) {
            log.warn("Initial administrator {} was not created because ADMIN_PASSWORD is not set", email);
            return;
        }
        if (userRepository.existsByLoginIdentifier(email)) {
            log.info("Initial administrator {} already exists; leaving the account unchanged", email);
            return;
        }
        AppUser admin = new AppUser();
        admin.setFullName(fullName);
        admin.setLoginIdentifier(email);
        admin.setPhone("Not provided");
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(AppRole.ADMIN);
        userRepository.save(admin);
        log.info("Created initial administrator account {}", email);
    }
}