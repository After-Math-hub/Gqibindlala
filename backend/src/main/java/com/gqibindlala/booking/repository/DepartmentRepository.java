package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByActiveTrueAndCatalogVisibleTrueOrderByNameAsc();
    boolean existsBySlug(String slug);
    boolean existsByLoginDomain(String loginDomain);
    Optional<Department> findBySlug(String slug);
}