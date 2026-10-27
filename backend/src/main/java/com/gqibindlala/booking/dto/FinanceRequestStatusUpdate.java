package com.gqibindlala.booking.dto;

import com.gqibindlala.booking.model.FinanceRequestStatus;
import jakarta.validation.constraints.NotNull;

public record FinanceRequestStatusUpdate(@NotNull FinanceRequestStatus status) {}