package com.gqibindlala.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(OutputCaptureExtension.class)
class BrevoEmailServiceTest {
    private MockRestServiceServer server;
    private BrevoEmailService emailService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        emailService = new BrevoEmailService(
            builder, new ObjectMapper(), "test-api-key", "verified@example.com", "Gqibindlala");
    }

    @Test
    void sendsVerificationEmailThroughBrevo() {
        String verificationUrl = "https://example.com/verify-email?token=abc123";
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("api-key", "test-api-key"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.sender.email").value("verified@example.com"))
            .andExpect(jsonPath("$.sender.name").value("Gqibindlala"))
            .andExpect(jsonPath("$.to[0].email").value("customer@example.com"))
            .andExpect(jsonPath("$.subject").value("Verify your Gqibindlala account"))
            .andExpect(jsonPath("$.htmlContent").value(containsString(verificationUrl)))
            .andRespond(withSuccess());

        emailService.sendVerification("customer@example.com", verificationUrl);

        server.verify();
    }

    @Test
    void logsBrevoStatusAndErrorWithoutLoggingApiKey(CapturedOutput output) {
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                .withStatus(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"unauthorized\",\"message\":\"Key not found\"}"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> emailService.sendVerification("customer@example.com", "https://example.com/verify?token=secret"));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertTrue(output.getAll()
            .contains("httpStatus=401, brevoErrorCode=unauthorized, brevoErrorMessage=Key not found"));
        assertTrue(output.getAll().contains("senderEmail=verified@example.com"));
        assertTrue(output.getAll().contains("apiKeyConfigured=true"));
        assertTrue(output.getAll().contains("requestReachedBrevo=true"));
        assertFalse(output.getAll().contains("test-api-key"));
        assertFalse(output.getAll().contains("token=secret"));
        server.verify();
    }

    @Test
    void logsMissingConfigurationWithoutAttemptingRequest(CapturedOutput output) {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        emailService = new BrevoEmailService(builder, new ObjectMapper(), "", "", "Gqibindlala");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> emailService.sendVerification("customer@example.com", "https://example.com/verify"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertTrue(output.getAll().contains("apiKeyConfigured=false"));
        assertTrue(output.getAll().contains("requestStarted=false"));
        assertTrue(output.getAll().contains("requestReachedBrevo=false"));
        server.verify();
    }

    @Test
    void reportsWhenRequestFailsWithoutBrevoResponse(CapturedOutput output) throws IOException {
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
            .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                .withException(new IOException("Connection refused")));

        assertThrows(ResponseStatusException.class,
            () -> emailService.sendVerification("customer@example.com", "https://example.com/verify"));

        assertTrue(output.getAll().contains("brevoResponseReceived=false, requestReachedBrevo=unknown"));
        assertTrue(output.getAll().contains("httpStatus=unavailable"));
        assertFalse(output.getAll().contains("test-api-key"));
        server.verify();
    }
}