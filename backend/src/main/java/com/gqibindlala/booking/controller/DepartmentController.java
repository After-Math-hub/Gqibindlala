package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.CreateDepartmentRequest;
import com.gqibindlala.booking.dto.DepartmentResponse;
import com.gqibindlala.booking.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public List<DepartmentResponse> activeDepartments() {
        return departmentService.activeDepartments().stream().map(DepartmentResponse::from).toList();
    }

    @GetMapping("/admin")
    public List<DepartmentResponse> allDepartments() {
        return departmentService.allDepartments().stream().map(DepartmentResponse::from).toList();
    }

    @PostMapping
    public DepartmentResponse create(@Valid @RequestBody CreateDepartmentRequest request) {
        return DepartmentResponse.from(departmentService.create(request));
    }

    @PatchMapping("/{id}/active")
    public DepartmentResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return DepartmentResponse.from(departmentService.setActive(id, active));
    }
}