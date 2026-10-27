package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.InventoryChangeRequest;
import com.gqibindlala.booking.model.InventoryChangeStatus;
import com.gqibindlala.booking.model.InventoryChangeType;
import java.math.BigDecimal;
import java.time.Instant;

public record InventoryChangeResponse(Long id, InventoryChangeType changeType, InventoryChangeStatus status,
                                      Long departmentId, String departmentName, Long equipmentId,
                                      String equipmentName, String proposedName, Integer proposedQuantity,
                                      BigDecimal proposedPrice, String proposedImageUrl, String reason,
                                      String requestedBy, String reviewedBy, String reviewNote,
                                      Instant createdAt, Instant reviewedAt) {
    public static InventoryChangeResponse from(InventoryChangeRequest request) {
        return new InventoryChangeResponse(request.getId(), request.getChangeType(), request.getStatus(),
            request.getDepartment().getId(), request.getDepartment().getName(),
            request.getEquipment() == null ? null : request.getEquipment().getId(),
            request.getEquipment() == null ? null : request.getEquipment().getName(),
            request.getProposedName(), request.getProposedQuantity(), request.getProposedPrice(),
            request.getProposedImageUrl(), request.getReason(), request.getRequestedBy().getFullName(),
            request.getReviewedBy() == null ? null : request.getReviewedBy().getFullName(),
            request.getReviewNote(), request.getCreatedAt(), request.getReviewedAt());
    }
}