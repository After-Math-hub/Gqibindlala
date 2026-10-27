package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.CreateStaffRequest;
import com.gqibindlala.booking.dto.UserResponse;
import com.gqibindlala.booking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final AuthService authService;

    public StaffController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateStaffRequest request) {
        return authService.createStaff(request);
    }
}