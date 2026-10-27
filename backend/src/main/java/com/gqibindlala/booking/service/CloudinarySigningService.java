package com.gqibindlala.booking.service;

import com.gqibindlala.booking.dto.CloudinaryUploadSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class CloudinarySigningService {
    private static final String FOLDER = "gqibindlala/equipment";
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinarySigningService(@Value("${cloudinary.cloud-name:}") String cloudName,
                                    @Value("${cloudinary.api-key:}") String apiKey,
                                    @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public CloudinaryUploadSignature createUploadSignature() {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Equipment image uploads are not configured");
        }
        long timestamp = Instant.now().getEpochSecond();
        String signature = cloudinarySignature("folder=" + FOLDER + "&timestamp=" + timestamp, apiSecret);
        return new CloudinaryUploadSignature(cloudName, apiKey, timestamp, FOLDER, signature);
    }

    private String cloudinarySignature(String value, String secret) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-1")
                .digest((value + secret).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Cloudinary request signing failed", exception);
        }
    }
}