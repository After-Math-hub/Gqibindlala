package com.gqibindlala.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderMessageRequest(@NotBlank @Size(max = 4000) String content) {}