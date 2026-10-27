package com.gqibindlala.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewInventoryChangeRequest(@NotNull Boolean approve, @Size(max = 1000) String reviewNote) {}