package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.AppRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStaffRequest(
    @NotBlank @Size(max = 255) String fullName,
    @Email @Size(max = 255) String personalEmail,
    @NotBlank @Size(max = 255) String phone,
    @NotBlank @Size(min = 10, max = 72) String temporaryPassword,
    @NotNull AppRole role,
    Long departmentId
) {}