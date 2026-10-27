package com.gqibindlala.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInventoryChangeMessageRequest(@NotBlank @Size(max = 4000) String content) {}