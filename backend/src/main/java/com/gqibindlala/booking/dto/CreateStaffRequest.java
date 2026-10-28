package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.AppRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStaffRequest(
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Email @Size(max = 255) String personalEmail,
    @NotBlank @Size(max = 255) String phone,
    @NotNull AppRole role,
    Long departmentId
) {}