package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.AuthResponse;
import com.gqibindlala.booking.dto.LoginRequest;
import com.gqibindlala.booking.dto.RegisterRequest;
import com.gqibindlala.booking.dto.UserResponse;
import com.gqibindlala.booking.security.AuthPrincipal;
import com.gqibindlala.booking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
        return new UserResponse(principal.id(), principal.fullName(), principal.loginIdentifier(),
            principal.personalEmail(), principal.phone(), principal.role(), principal.departmentId(),
            principal.departmentName());
    }
}