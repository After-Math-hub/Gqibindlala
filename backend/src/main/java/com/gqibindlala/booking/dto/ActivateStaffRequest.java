package com.gqibindlala.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateStaffRequest(
    @NotBlank @Size(max = 128) String token,
    @NotBlank @Size(min = 10, max = 72) String password
) {}