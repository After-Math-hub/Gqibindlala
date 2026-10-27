package com.gqibindlala.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentReferenceRequest(@NotBlank @Size(max = 255) String reference) {}