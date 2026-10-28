package com.gqibindlala.booking.service;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class BrevoEmailServiceTest {
    private MockRestServiceServer server;
    private BrevoEmailService emailService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        emailService = new BrevoEmailService(builder, "test-api-key", "verified@example.com", "Gqibindlala");
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
}