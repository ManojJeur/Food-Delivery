package com.fooddelivery.order.dto.request;
import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
public record CartItemRequest(@NotNull UUID menuItemId, @Min(1) @Max(99) int quantity) { }
