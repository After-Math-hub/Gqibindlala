package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.Equipment;
import java.math.BigDecimal;

public record EquipmentResponse(Long id, String name, String description, Integer totalQuantity,
                                BigDecimal pricePerUnitPerDay, String imageUrl,
                                Long departmentId, String departmentName, String departmentSlug) {
    public static EquipmentResponse from(Equipment equipment) {
        return new EquipmentResponse(equipment.getId(), equipment.getName(), equipment.getDescription(),
            equipment.getTotalQuantity(), equipment.getPricePerUnitPerDay(), equipment.getImageUrl(),
            equipment.getDepartment().getId(), equipment.getDepartment().getName(), equipment.getDepartment().getSlug());
    }
}