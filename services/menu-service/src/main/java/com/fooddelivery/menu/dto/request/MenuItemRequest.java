package com.fooddelivery.menu.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public record MenuItemRequest(
        @NotNull UUID restaurantId,
        @NotNull UUID categoryId,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 2000) String description,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price,
        @Size(max = 2048) String imageUrl,
        boolean vegetarian,
        @Positive @Max(240) int preparationTimeMinutes) {
}
