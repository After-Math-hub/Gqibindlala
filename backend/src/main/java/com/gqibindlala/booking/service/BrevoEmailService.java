package com.gqibindlala.booking.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BrevoEmailService {
    private final RestClient restClient;
    private final String apiKey;
    private final String from;
    private final String senderName;

    public BrevoEmailService(RestClient.Builder restClientBuilder,
                             @Value("${brevo.api-key:}") String apiKey,
                             @Value("${brevo.from:}") String from,
                             @Value("${brevo.sender-name:Gqibindlala}") String senderName) {
        this.restClient = restClientBuilder.baseUrl("https://api.brevo.com").build();
        this.apiKey = apiKey;
        this.from = from;
        this.senderName = senderName;
    }

    public void sendVerification(String recipient, String verificationUrl) {
        if (apiKey.isBlank() || from.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Customer email verification is not configured");
        }
        String html = "<p>Verify your Gqibindlala account by opening this link:</p>"
            + "<p><a href=\"" + verificationUrl + "\">Verify my email address</a></p>"
            + "<p>This link expires in 24 hours. If you did not create an account, ignore this email.</p>";
        send(recipient, "Verify your Gqibindlala account", html);
    }

    public void sendStaffActivation(String recipient, String loginIdentifier, String activationUrl) {
        String html = "<p>Your Gqibindlala staff account is ready.</p>"
            + "<p>Your site login is <strong>" + loginIdentifier + "</strong>.</p>"
            + "<p><a href=\"" + activationUrl + "\">Activate your account and set a password</a></p>"
            + "<p>This one-time link expires in 24 hours. If you did not expect this email, ignore it.</p>";
        send(recipient, "Activate your Gqibindlala staff account", html);
    }

    private void send(String recipient, String subject, String html) {
        if (apiKey.isBlank() || from.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Email delivery is not configured");
        }
        try {
            restClient.post()
                .uri("/v3/smtp/email")
                .header("api-key", apiKey)
                .body(Map.of("sender", Map.of("name", senderName, "email", from),
                    "to", List.of(Map.of("email", recipient)),
                    "subject", subject, "htmlContent", html))
                .retrieve()
                .toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Email could not be sent");
        }
    }
}