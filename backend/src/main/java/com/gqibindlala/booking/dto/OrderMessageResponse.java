package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.OrderMessage;
import java.time.Instant;

public record OrderMessageResponse(Long id, Long bookingId, String senderName, AppRole senderRole,
                                   Long departmentId, String departmentName, String content, Instant createdAt) {
    public static OrderMessageResponse from(OrderMessage message) {
        return new OrderMessageResponse(message.getId(), message.getBookingRequest().getId(),
            message.getSender().getFullName(), message.getSender().getRole(),
            message.getDepartment() == null ? null : message.getDepartment().getId(),
            message.getDepartment() == null ? null : message.getDepartment().getName(),
            message.getContent(), message.getCreatedAt());
    }
}