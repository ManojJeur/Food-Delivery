package com.fooddelivery.menu.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MenuCategoryResponse(UUID id, UUID restaurantId, String name, String description, int displayOrder,
        boolean active, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
