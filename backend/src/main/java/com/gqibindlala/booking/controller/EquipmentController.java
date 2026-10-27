package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.EquipmentRequest;
import com.gqibindlala.booking.dto.EquipmentResponse;
import com.gqibindlala.booking.model.Department;
import com.gqibindlala.booking.model.Equipment;
import com.gqibindlala.booking.repository.DepartmentRepository;
import com.gqibindlala.booking.repository.EquipmentRepository;
import com.gqibindlala.booking.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;
    private final DepartmentRepository departmentRepository;
    private final AvailabilityService availabilityService;

    public EquipmentController(EquipmentRepository equipmentRepository,
                                DepartmentRepository departmentRepository,
                                AvailabilityService availabilityService) {
        this.equipmentRepository = equipmentRepository;
        this.departmentRepository = departmentRepository;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public List<EquipmentResponse> listAll() {
        return equipmentRepository.findByDepartmentActiveTrue().stream().map(EquipmentResponse::from).toList();
    }

    @GetMapping("/admin")
    public List<EquipmentResponse> listAllForAdmin() {
        return equipmentRepository.findAll().stream().map(EquipmentResponse::from).toList();
    }

    @GetMapping("/{id}/availability")
    public Map<String, Object> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        int remaining = availabilityService.remainingQuantity(id, startDate, endDate);
        return Map.of("equipmentId", id, "startDate", startDate, "endDate", endDate, "remaining", remaining);
    }

    // --- admin-only ---

    @PostMapping
    public EquipmentResponse create(@Valid @RequestBody EquipmentRequest request) {
        Equipment equipment = new Equipment();
        applyRequest(equipment, request);
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    @PutMapping("/{id}")
    public EquipmentResponse update(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        Equipment existing = equipmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + id));
        applyRequest(existing, request);
        return EquipmentResponse.from(equipmentRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        equipmentRepository.deleteById(id);
    }

    private void applyRequest(Equipment equipment, EquipmentRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
            .filter(Department::isActive)
            .orElseThrow(() -> new IllegalArgumentException("Active department not found: " + request.departmentId()));
        if (department.getSlug().equals("finance")) {
            throw new IllegalArgumentException("Inventory cannot be assigned to Finance");
        }
        equipment.setName(request.name().trim());
        equipment.setDepartment(department);
        equipment.setDescription(request.description());
        equipment.setTotalQuantity(request.totalQuantity());
        equipment.setPricePerUnitPerDay(request.pricePerUnitPerDay());
        equipment.setImageUrl(request.imageUrl());
    }
}
