package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.AuthResponse;
import com.gqibindlala.booking.dto.LoginRequest;
import com.gqibindlala.booking.dto.RegisterRequest;
import com.gqibindlala.booking.dto.RegistrationResponse;
import com.gqibindlala.booking.dto.UserResponse;
import com.gqibindlala.booking.dto.VerifyEmailRequest;
import com.gqibindlala.booking.security.AuthPrincipal;
import com.gqibindlala.booking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public RegistrationResponse register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return new RegistrationResponse("Verification email sent. Please check your inbox.");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PutMapping("/verify-email")
    public RegistrationResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return new RegistrationResponse("Email verified. You can now sign in.");
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
        return new UserResponse(principal.id(), principal.fullName(), principal.loginIdentifier(),
            principal.personalEmail(), principal.phone(), principal.role(), principal.departmentId(),
            principal.departmentName(), true);
    }
}