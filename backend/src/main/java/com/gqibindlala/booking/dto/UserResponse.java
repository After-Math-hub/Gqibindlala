package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;

public record UserResponse(Long id, String fullName, String loginIdentifier, String personalEmail,
                           String phone, AppRole role, Long departmentId, String departmentName,
                           boolean active) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getLoginIdentifier(),
            user.getPersonalEmail(), user.getPhone(), user.getRole(),
            user.getDepartment() == null ? null : user.getDepartment().getId(),
            user.getDepartment() == null ? null : user.getDepartment().getName(), user.isActive());
    }
}