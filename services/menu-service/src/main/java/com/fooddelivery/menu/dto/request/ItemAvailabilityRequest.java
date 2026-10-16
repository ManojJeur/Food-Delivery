package com.fooddelivery.menu.dto.request;

import jakarta.validation.constraints.NotNull;

public record ItemAvailabilityRequest(@NotNull Boolean available) {
}
