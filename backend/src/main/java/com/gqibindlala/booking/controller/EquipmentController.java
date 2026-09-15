package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.model.Equipment;
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
    private final AvailabilityService availabilityService;

    public EquipmentController(EquipmentRepository equipmentRepository,
                                AvailabilityService availabilityService) {
        this.equipmentRepository = equipmentRepository;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public List<Equipment> listAll() {
        return equipmentRepository.findAll();
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
    public Equipment create(@Valid @RequestBody Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    @PutMapping("/{id}")
    public Equipment update(@PathVariable Long id, @Valid @RequestBody Equipment updated) {
        Equipment existing = equipmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + id));
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setDescription(updated.getDescription());
        existing.setTotalQuantity(updated.getTotalQuantity());
        existing.setPricePerUnitPerDay(updated.getPricePerUnitPerDay());
        existing.setImageUrl(updated.getImageUrl());
        return equipmentRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        equipmentRepository.deleteById(id);
    }
}
