package com.fooddelivery.order.client;
import java.math.BigDecimal;
import java.util.UUID;
public record MenuItemQuote(UUID id, UUID restaurantId, String name, BigDecimal price, boolean vegetarian) { }
