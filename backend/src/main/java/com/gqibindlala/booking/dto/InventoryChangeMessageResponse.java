package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.InventoryChangeMessage;
import java.time.Instant;

public record InventoryChangeMessageResponse(Long id, Long changeRequestId, String senderName,
                                             AppRole senderRole, String content, Instant createdAt) {
    public static InventoryChangeMessageResponse from(InventoryChangeMessage message) {
        return new InventoryChangeMessageResponse(message.getId(), message.getChangeRequest().getId(),
            message.getSender().getFullName(), message.getSender().getRole(), message.getContent(),
            message.getCreatedAt());
    }
}