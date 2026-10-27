package com.gqibindlala.booking.service;

import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.EmailVerificationToken;
import com.gqibindlala.booking.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class ResendEmailVerificationService implements EmailVerificationService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final ResendEmailService emailService;
    private final String publicUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public ResendEmailVerificationService(EmailVerificationTokenRepository tokenRepository,
                                          ResendEmailService emailService,
                                          @Value("${app.public-url:http://localhost:5173}") String publicUrl) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.publicUrl = publicUrl.replaceAll("/$", "");
    }

    @Override
    @Transactional
    public void sendVerification(AppUser user) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setTokenHash(hash(token));
        verificationToken.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        tokenRepository.save(verificationToken);
        emailService.sendVerification(user.getPersonalEmail(), publicUrl + "/verify-email?token=" + token);
    }

    @Override
    @Transactional
    public void verify(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByTokenHashAndUsedAtIsNull(hash(token))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification link is invalid or already used"));
        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification link has expired");
        }
        verificationToken.getUser().setEmailVerified(true);
        verificationToken.setUsedAt(Instant.now());
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
