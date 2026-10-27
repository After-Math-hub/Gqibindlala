package com.gqibindlala.booking.controller;

import com.gqibindlala.booking.dto.EquipmentResponse;
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
    public List<EquipmentResponse> listAll() {
        return equipmentRepository.findByDepartmentActiveTrue().stream().map(EquipmentResponse::from).toList();
    }

    @GetMapping("/{id}/availability")
    public Map<String, Object> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        int remaining = availabilityService.remainingQuantity(id, startDate, endDate);
        return Map.of("equipmentId", id, "startDate", startDate, "endDate", endDate, "remaining", remaining);
    }

}
