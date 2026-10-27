package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.Department;

public record DepartmentResponse(Long id, String name, String slug, String loginDomain, boolean active,
                                 boolean catalogVisible) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getName(), department.getSlug(),
            department.getLoginDomain(), department.isActive(), department.isCatalogVisible());
    }
}