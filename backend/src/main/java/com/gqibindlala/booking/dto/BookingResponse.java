package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.BookingRequest;
import com.gqibindlala.booking.model.BookingStatus;
import com.gqibindlala.booking.model.PaymentStatus;
import java.time.Instant;
import java.util.List;

public record BookingResponse(Long id, String customerName, String customerContact, BookingStatus status,
                              PaymentStatus paymentStatus, String paymentReference, Instant createdAt,
                              List<BookingItemResponse> items) {
    public static BookingResponse from(BookingRequest booking) {
        return from(booking, true, null);
    }

    public static BookingResponse forDepartment(BookingRequest booking, Long departmentId) {
        return from(booking, false, departmentId);
    }

    private static BookingResponse from(BookingRequest booking, boolean includePaymentReference,
                                        Long departmentId) {
        List<BookingItemResponse> items = booking.getItems().stream()
            .filter(item -> departmentId == null || item.getEquipment().getDepartment().getId().equals(departmentId))
            .map(BookingItemResponse::from)
            .toList();
        return new BookingResponse(booking.getId(), booking.getCustomerName(), booking.getCustomerContact(),
            booking.getStatus(), booking.getPaymentStatus(),
            includePaymentReference ? booking.getPaymentReference() : null,
            booking.getCreatedAt(), items);
    }
}