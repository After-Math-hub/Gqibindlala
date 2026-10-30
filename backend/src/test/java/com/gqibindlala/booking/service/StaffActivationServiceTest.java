package com.gqibindlala.booking.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.StaffActivationToken;
import com.gqibindlala.booking.repository.AppUserRepository;
import com.gqibindlala.booking.repository.StaffActivationTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class StaffActivationServiceTest {
    private AppUserRepository userRepository;
    private StaffActivationTokenRepository tokenRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private StaffActivationService activationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(AppUserRepository.class);
        tokenRepository = mock(StaffActivationTokenRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        activationService = new StaffActivationService(userRepository, tokenRepository,
            new BrevoEmailService(org.springframework.web.client.RestClient.builder(), new ObjectMapper(),
                "", "", "Gqibindlala"),
            passwordEncoder, "http://localhost:5173");
    }

    @Test
    void validTokenSetsPasswordAndActivatesStaffOnce() throws Exception {
        AppUser user = pendingStaff();
        StaffActivationToken token = tokenFor(user, Instant.now().plus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByTokenHashAndUsedAtIsNull(anyString()))
            .thenReturn(Optional.of(token), Optional.empty());

        activationService.activate("one-time-token", "a-secure-new-password");

        assertTrue(user.isActive());
        assertTrue(user.isStaffActivated());
        assertTrue(passwordEncoder.matches("a-secure-new-password", user.getPasswordHash()));
        assertTrue(token.getUsedAt() != null);
        assertThrows(ResponseStatusException.class,
            () -> activationService.activate("one-time-token", "another-secure-password"));
    }

    @Test
    void expiredTokenDoesNotActivateStaff() throws Exception {
        AppUser user = pendingStaff();
        StaffActivationToken token = tokenFor(user, Instant.now().minus(1, ChronoUnit.SECONDS));
        when(tokenRepository.findByTokenHashAndUsedAtIsNull(anyString())).thenReturn(Optional.of(token));

        assertThrows(ResponseStatusException.class,
            () -> activationService.activate("one-time-token", "a-secure-new-password"));

        assertFalse(user.isActive());
        assertFalse(user.isStaffActivated());
        assertTrue(token.getUsedAt() == null);
    }

    private AppUser pendingStaff() {
        AppUser user = new AppUser();
        user.setRole(AppRole.DEPARTMENT);
        user.setActive(false);
        user.setStaffActivated(false);
        user.setPasswordHash(passwordEncoder.encode("random-placeholder-password"));
        return user;
    }

    private StaffActivationToken tokenFor(AppUser user, Instant expiresAt) throws Exception {
        StaffActivationToken token = new StaffActivationToken();
        token.setUser(user);
        token.setTokenHash(hash("one-time-token"));
        token.setExpiresAt(expiresAt);
        return token;
    }

    private String hash(String token) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}