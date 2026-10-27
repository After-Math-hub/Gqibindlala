package com.gqibindlala.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CreateBookingRequest {

    @NotEmpty
    @Valid
    private List<BookingItemRequest> items;

    public List<BookingItemRequest> getItems() { return items; }
    public void setItems(List<BookingItemRequest> items) { this.items = items; }
}
