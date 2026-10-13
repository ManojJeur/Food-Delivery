package com.fooddelivery.restaurant.dto.request;

import com.fooddelivery.restaurant.entity.RestaurantStatus;
import jakarta.validation.constraints.NotNull;

public record RestaurantStatusRequest(@NotNull RestaurantStatus status) {
}
