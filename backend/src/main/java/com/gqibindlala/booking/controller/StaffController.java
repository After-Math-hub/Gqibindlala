package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.CreateStaffRequest;
import com.gqibindlala.booking.dto.RegistrationResponse;
import com.gqibindlala.booking.dto.UserResponse;
import com.gqibindlala.booking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public java.util.List<UserResponse> list() {
        return authService.staffUsers();
    }

    @PatchMapping("/{id}/active")
    public UserResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return authService.setStaffActive(id, active);
    }

    @PostMapping("/{id}/activation")
    public RegistrationResponse resendActivation(@PathVariable Long id) {
        authService.resendStaffActivation(id);
        return new RegistrationResponse("Activation email sent.");
    }
}