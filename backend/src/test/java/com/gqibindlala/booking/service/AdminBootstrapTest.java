package com.gqibindlala.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AdminBootstrapTest {
    private AppUserRepository userRepository;
    private ClaimingJdbcTemplate jdbcTemplate;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(AppUserRepository.class);
        jdbcTemplate = new ClaimingJdbcTemplate();
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void createsConfiguredAdminWithEncodedPassword() {
        when(userRepository.existsByLoginIdentifier("admin@example.com")).thenReturn(false);
        AdminBootstrap bootstrap = new AdminBootstrap(
            userRepository, jdbcTemplate, passwordEncoder, " Admin@Example.com ", "a-strong-password",
            "System Administrator", false);

        bootstrap.run(new DefaultApplicationArguments());

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        AppUser admin = captor.getValue();
        assertEquals("admin@example.com", admin.getLoginIdentifier());
        assertEquals("System Administrator", admin.getFullName());
        assertEquals(AppRole.ADMIN, admin.getRole());
        assertTrue(admin.isActive());
        assertTrue(passwordEncoder.matches("a-strong-password", admin.getPasswordHash()));
    }

    @Test
    void leavesExistingAccountUnchanged() {
        AppUser admin = adminAccount();
        admin.setPasswordHash(passwordEncoder.encode("old-password"));
        when(userRepository.findByLoginIdentifier("admin@example.com")).thenReturn(java.util.Optional.of(admin));
        AdminBootstrap bootstrap = new AdminBootstrap(
            userRepository, jdbcTemplate, passwordEncoder, "admin@example.com", "a-strong-password",
            "System Administrator", false);

        bootstrap.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(AppUser.class));
        assertEquals(0, jdbcTemplate.claims);
    }

    @Test
    void leavesExistingAdminUnchangedWhenConfiguredPasswordAlreadyMatches() {
        AppUser admin = adminAccount();
        admin.setPasswordHash(passwordEncoder.encode("a-strong-password"));
        when(userRepository.findByLoginIdentifier("admin@example.com")).thenReturn(java.util.Optional.of(admin));
        AdminBootstrap bootstrap = new AdminBootstrap(
            userRepository, jdbcTemplate, passwordEncoder, "admin@example.com", "a-strong-password",
            "System Administrator", true);

        bootstrap.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(AppUser.class));
        assertEquals(1, jdbcTemplate.claims);
    }

    @Test
    void resetsExistingAdminPasswordOnlyWhenEnabledAndGuardIsClaimed() {
        AppUser admin = adminAccount();
        admin.setPasswordHash(passwordEncoder.encode("old-password"));
        when(userRepository.findByLoginIdentifier("admin@example.com")).thenReturn(java.util.Optional.of(admin));
        jdbcTemplate.claimResult = 1;
        AdminBootstrap bootstrap = new AdminBootstrap(
            userRepository, jdbcTemplate, passwordEncoder, "admin@example.com", "new-strong-password",
            "System Administrator", true);

        bootstrap.run(new DefaultApplicationArguments());

        verify(userRepository).save(admin);
        assertTrue(passwordEncoder.matches("new-strong-password", admin.getPasswordHash()));
    }

    @Test
    void doesNotResetExistingAdminPasswordAfterGuardWasConsumed() {
        AppUser admin = adminAccount();
        admin.setPasswordHash(passwordEncoder.encode("old-password"));
        String oldHash = admin.getPasswordHash();
        when(userRepository.findByLoginIdentifier("admin@example.com")).thenReturn(java.util.Optional.of(admin));
        jdbcTemplate.claimResult = 0;
        AdminBootstrap bootstrap = new AdminBootstrap(
            userRepository, jdbcTemplate, passwordEncoder, "admin@example.com", "new-strong-password",
            "System Administrator", true);

        bootstrap.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(AppUser.class));
        assertEquals(oldHash, admin.getPasswordHash());
    }

    @Test
    void doesNotCreateAdminWithoutConfiguredPassword() {
        AdminBootstrap bootstrap = new AdminBootstrap(
            userRepository, jdbcTemplate, passwordEncoder, "admin@example.com", " ", "System Administrator", false);

        bootstrap.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(AppUser.class));
    }

    private AppUser adminAccount() {
        AppUser admin = new AppUser();
        admin.setLoginIdentifier("admin@example.com");
        admin.setFullName("System Administrator");
        admin.setPhone("Not provided");
        admin.setRole(AppRole.ADMIN);
        return admin;
    }

    private static class ClaimingJdbcTemplate extends JdbcTemplate {
        private int claimResult;
        private int claims;

        @Override
        public int update(String sql) {
            claims++;
            return claimResult;
        }
    }
}
