package com.gqibindlala.booking.service;

import com.gqibindlala.booking.dto.CreateDepartmentRequest;
import com.gqibindlala.booking.model.Department;
import com.gqibindlala.booking.repository.DepartmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Department> activeDepartments() {
        return departmentRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Department> allDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department create(CreateDepartmentRequest request) {
        String slug = slugify(request.name());
        String loginDomain = request.loginDomain().trim().toLowerCase(Locale.ROOT);
        if (departmentRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A department with this name already exists");
        }
        if (departmentRepository.existsByLoginDomain(loginDomain)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This login domain is already in use");
        }
        Department department = new Department();
        department.setName(request.name().trim());
        department.setSlug(slug);
        department.setLoginDomain(loginDomain);
        return departmentRepository.save(department);
    }

    @Transactional
    public Department setActive(Long id, boolean active) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        if (!active && department.getSlug().equals("finance")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Finance is required for payment review");
        }
        department.setActive(active);
        return departmentRepository.save(department);
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
        if (normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name must contain letters or numbers");
        }
        return normalized;
    }
}