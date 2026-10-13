package com.fooddelivery.restaurant.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import com.fooddelivery.restaurant.entity.RestaurantStatus;

public record RestaurantResponse(UUID id, String name, String description, String phone, String email,
        String address, BigDecimal latitude, BigDecimal longitude, RestaurantStatus status,
        LocalTime openingTime, LocalTime closingTime, Instant createdAt, Instant updatedAt) {
}
