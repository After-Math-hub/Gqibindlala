package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.InventoryChangeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record InventoryChangeRequestInput(
    @NotNull InventoryChangeType changeType,
    Long equipmentId,
    @Size(max = 255) String name,
    @Size(max = 1000) String description,
    Integer quantity,
    BigDecimal pricePerUnitPerDay,
    @Size(max = 500) String imageUrl,
    @NotBlank @Size(max = 1000) String reason
) {}