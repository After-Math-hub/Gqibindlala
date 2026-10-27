package com.gqibindlala.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 255) @Pattern(regexp = "(?i)[a-z0-9.-]+\\.[a-z]{2,}") String loginDomain
) {}