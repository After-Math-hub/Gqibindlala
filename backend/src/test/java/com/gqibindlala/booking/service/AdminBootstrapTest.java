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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AdminBootstrapTest {
    private AppUserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(AppUserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void createsConfiguredAdminWithEncodedPassword() {
        when(userRepository.existsByLoginIdentifier("admin@example.com")).thenReturn(false);
        AdminBootstrap bootstrap = new AdminBootstrap(
            userRepository, passwordEncoder, " Admin@Example.com ", "a-strong-password", "System Administrator");

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
        when(userRepository.existsByLoginIdentifier("admin@example.com")).thenReturn(true);
        AdminBootstrap bootstrap = new AdminBootstrap(
            userRepository, passwordEncoder, "admin@example.com", "a-strong-password", "System Administrator");

        bootstrap.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void doesNotCreateAdminWithoutConfiguredPassword() {
        AdminBootstrap bootstrap = new AdminBootstrap(
            userRepository, passwordEncoder, "admin@example.com", " ", "System Administrator");

        bootstrap.run(new DefaultApplicationArguments());

        verify(userRepository, never()).save(any(AppUser.class));
    }
}
