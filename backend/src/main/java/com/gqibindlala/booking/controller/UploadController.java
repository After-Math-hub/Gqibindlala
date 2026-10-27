package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.CloudinaryUploadSignature;
import com.gqibindlala.booking.service.CloudinarySigningService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final CloudinarySigningService signingService;

    public UploadController(CloudinarySigningService signingService) {
        this.signingService = signingService;
    }

    @PostMapping("/cloudinary-signature")
    public CloudinaryUploadSignature cloudinarySignature() {
        return signingService.createUploadSignature();
    }
}