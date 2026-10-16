package com.fooddelivery.menu.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuCategoryRequest(
        @NotNull UUID restaurantId,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @Min(0) int displayOrder) {
}
