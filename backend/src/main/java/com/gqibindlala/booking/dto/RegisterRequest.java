package com.gqibindlala.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(max = 255) String phone,
    @NotBlank @Size(min = 10, max = 72) String password
) {}