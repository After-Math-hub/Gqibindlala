package com.gqibindlala.booking.service;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BrevoEmailService {
    private static final Logger log = LoggerFactory.getLogger(BrevoEmailService.class);
    private static final String ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String from;
    private final String senderName;

    public BrevoEmailService(RestClient.Builder restClientBuilder,
                             ObjectMapper objectMapper,
                             @Value("${brevo.api-key:}") String apiKey,
                             @Value("${brevo.from:}") String from,
                             @Value("${brevo.sender-name:Gqibindlala}") String senderName) {
        this.restClient = restClientBuilder.baseUrl("https://api.brevo.com").build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.from = from;
        this.senderName = senderName;
    }

    public void sendVerification(String recipient, String verificationUrl) {
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
        boolean apiKeyConfigured = apiKey != null && !apiKey.isBlank();
        boolean senderConfigured = from != null && !from.isBlank();
        if (!apiKeyConfigured || !senderConfigured) {
            log.warn("Brevo email not sent: reason=configuration_missing, endpoint={}, "
                    + "apiKeyConfigured={}, senderEmail={}, senderName={}, requestStarted=false, "
                    + "brevoResponseReceived=false, requestReachedBrevo=false",
                ENDPOINT, apiKeyConfigured, safeLogValue(from), safeLogValue(senderName));
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Email delivery is not configured");
        }
        try {
            ResponseEntity<Void> response = restClient.post()
                .uri("/v3/smtp/email")
                .header("api-key", apiKey)
                .body(Map.of("sender", Map.of("name", senderName, "email", from),
                    "to", List.of(Map.of("email", recipient)),
                    "subject", subject, "htmlContent", html))
                .retrieve()
                .toBodilessEntity();
            log.info("Brevo email accepted: endpoint={}, httpStatus={}, apiKeyConfigured=true, "
                    + "senderEmail={}, senderName={}, requestStarted=true, brevoResponseReceived=true, "
                    + "requestReachedBrevo=true",
                ENDPOINT, response.getStatusCode().value(), safeLogValue(from), safeLogValue(senderName));
        } catch (RestClientResponseException exception) {
            BrevoError error = parseError(exception.getResponseBodyAsString());
            log.error("Brevo email rejected: endpoint={}, httpStatus={}, brevoErrorCode={}, "
                    + "brevoErrorMessage={}, apiKeyConfigured=true, senderEmail={}, senderName={}, "
                    + "requestStarted=true, brevoResponseReceived=true, requestReachedBrevo=true",
                ENDPOINT, exception.getStatusCode().value(), error.code(), error.message(),
                safeLogValue(from), safeLogValue(senderName));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Email could not be sent");
        } catch (RestClientException exception) {
            log.error("Brevo email request failed without an HTTP response: endpoint={}, "
                    + "httpStatus=unavailable, brevoErrorCode=unavailable, brevoErrorMessage={}, "
                    + "apiKeyConfigured=true, senderEmail={}, senderName={}, requestStarted=true, "
                    + "brevoResponseReceived=false, requestReachedBrevo=unknown",
                ENDPOINT, safeLogValue(exception.getMessage()), safeLogValue(from), safeLogValue(senderName));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Email could not be sent");
        }
    }

    private BrevoError parseError(String responseBody) {
        try {
            JsonNode response = objectMapper.readTree(responseBody);
            if (response == null) {
                return new BrevoError("unavailable", "Empty Brevo error response");
            }
            return new BrevoError(
                safeLogValue(response.path("code").asText("unavailable")),
                safeLogValue(response.path("message").asText("No error message returned")));
        } catch (JsonProcessingException exception) {
            log.warn("Could not parse Brevo error response ({}); omitting response body from logs",
                exception.getClass().getSimpleName());
            return new BrevoError("unavailable", "Unparseable Brevo error response");
        }
    }

    private String safeLogValue(String value) {
        if (value == null || value.isBlank()) {
            return "(not configured)";
        }
        String sanitized = value.replaceAll("[\\p{Cntrl}]", " ");
        return sanitized.length() > 500 ? sanitized.substring(0, 500) : sanitized;
    }

    private record BrevoError(String code, String message) {}
}