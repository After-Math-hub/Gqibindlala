package com.gqibindlala.booking.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResendEmailService {
    private final RestClient restClient;
    private final String apiKey;
    private final String from;

    public ResendEmailService(RestClient.Builder restClientBuilder,
                              @Value("${resend.api-key:}") String apiKey,
                              @Value("${resend.from:onboarding@resend.dev}") String from) {
        this.restClient = restClientBuilder.baseUrl("https://api.resend.com").build();
        this.apiKey = apiKey;
        this.from = from;
    }

    public void sendVerification(String recipient, String verificationUrl) {
        if (apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Customer email verification is not configured");
        }
        String html = "<p>Verify your Gqibindlala account by opening this link:</p>"
            + "<p><a href=\"" + verificationUrl + "\">Verify my email address</a></p>"
            + "<p>This link expires in 24 hours. If you did not create an account, ignore this email.</p>";
        try {
            restClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of("from", from, "to", List.of(recipient),
                    "subject", "Verify your Gqibindlala account", "html", html))
                .retrieve()
                .toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Verification email could not be sent");
        }
    }
}