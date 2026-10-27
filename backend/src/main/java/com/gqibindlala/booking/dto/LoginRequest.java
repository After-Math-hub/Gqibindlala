package com.gqibindlala.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String loginIdentifier, @NotBlank String password) {}