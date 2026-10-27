package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.BookingItem;
import com.gqibindlala.booking.model.FulfillmentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingItemResponse(Long id, Long equipmentId, String equipmentName, Long departmentId,
                                  String departmentName,
                                  Integer quantity, LocalDate startDate, LocalDate endDate,
                                  BigDecimal unitPrice, BigDecimal lineTotal,
                                  FulfillmentStatus fulfillmentStatus, java.time.Instant quotationIssuedAt,
                                  String quotationIssuedBy) {
    public static BookingItemResponse from(BookingItem item) {
        return new BookingItemResponse(item.getId(), item.getEquipment().getId(), item.getEquipment().getName(),
            item.getEquipment().getDepartment().getId(), item.getEquipment().getDepartment().getName(),
            item.getQuantity(), item.getStartDate(), item.getEndDate(),
            item.getUnitPrice(), item.getLineTotal(), item.getFulfillmentStatus(), item.getQuotationIssuedAt(),
            item.getQuotationIssuedBy() == null ? null : item.getQuotationIssuedBy().getFullName());
    }
}