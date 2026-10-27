package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.FulfillmentStatus;
import jakarta.validation.constraints.NotNull;

public record FulfillmentUpdateRequest(@NotNull FulfillmentStatus status) {}