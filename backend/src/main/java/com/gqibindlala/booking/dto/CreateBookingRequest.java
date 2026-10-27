package com.gqibindlala.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import com.gqibindlala.booking.model.CheckoutPaymentOption;

import java.util.List;

public class CreateBookingRequest {

    @NotEmpty
    @Valid
    private List<BookingItemRequest> items;

    private CheckoutPaymentOption paymentOption = CheckoutPaymentOption.FULL_PAYMENT;

    public List<BookingItemRequest> getItems() { return items; }
    public void setItems(List<BookingItemRequest> items) { this.items = items; }

    public CheckoutPaymentOption getPaymentOption() { return paymentOption; }
    public void setPaymentOption(CheckoutPaymentOption paymentOption) {
        this.paymentOption = paymentOption == null ? CheckoutPaymentOption.FULL_PAYMENT : paymentOption;
    }
}
