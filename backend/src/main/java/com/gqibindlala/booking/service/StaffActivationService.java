package com.gqibindlala.booking.service;

import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.StaffActivationToken;
import com.gqibindlala.booking.repository.AppUserRepository;
import com.gqibindlala.booking.repository.StaffActivationTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StaffActivationService implements StaffActivation {
    private final AppUserRepository userRepository;
    private final StaffActivationTokenRepository tokenRepository;
    private final BrevoEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final String publicUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public StaffActivationService(AppUserRepository userRepository,
                                  StaffActivationTokenRepository tokenRepository,
                                  BrevoEmailService emailService,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${app.public-url:http://localhost:5173}") String publicUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.publicUrl = publicUrl.replaceAll("/$", "");
    }

    @Transactional
    @Override
    public AppUser createPendingAccount(AppUser user) {
        user.setActive(false);
        user.setStaffActivated(false);
        user.setPasswordHash(passwordEncoder.encode(randomSecret()));
        AppUser savedUser = userRepository.save(user);
        sendActivation(savedUser);
        return savedUser;
    }

    @Transactional
    @Override
    public void sendActivation(AppUser user) {
        if (user.isStaffActivated()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff account has already been activated");
        }
        Instant now = Instant.now();
        tokenRepository.findAllByUser_IdAndUsedAtIsNull(user.getId())
            .forEach(token -> token.setUsedAt(now));

        String token = randomSecret();
        StaffActivationToken activationToken = new StaffActivationToken();
        activationToken.setUser(user);
        activationToken.setTokenHash(hash(token));
        activationToken.setExpiresAt(now.plus(24, ChronoUnit.HOURS));
        tokenRepository.save(activationToken);
        emailService.sendStaffActivation(user.getPersonalEmail(), user.getLoginIdentifier(),
            publicUrl + "/activate-staff?token=" + token);
    }

    @Transactional
    @Override
    public void activate(String token, String password) {
        StaffActivationToken activationToken = tokenRepository.findByTokenHashAndUsedAtIsNull(hash(token))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activation link is invalid or already used"));
        if (activationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activation link has expired");
        }
        AppUser user = activationToken.getUser();
        if (user.isStaffActivated() || !isStaff(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff account cannot be activated with this link");
        }
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStaffActivated(true);
        user.setActive(true);
        activationToken.setUsedAt(Instant.now());
    }

    private boolean isStaff(AppUser user) {
        return user.getRole() == AppRole.DEPARTMENT || user.getRole() == AppRole.PAYMENTS;
    }

    private String randomSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}