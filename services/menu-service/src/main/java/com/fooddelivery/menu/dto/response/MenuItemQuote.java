package com.fooddelivery.menu.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemQuote(UUID id, UUID restaurantId, String name, BigDecimal price, boolean vegetarian) {
}
