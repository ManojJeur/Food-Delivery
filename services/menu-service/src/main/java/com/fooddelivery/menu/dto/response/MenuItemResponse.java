package com.fooddelivery.menu.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MenuItemResponse(UUID id, UUID restaurantId, UUID categoryId, String name, String description,
        BigDecimal price, String imageUrl, boolean vegetarian, boolean available, int preparationTimeMinutes,
        boolean active, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
