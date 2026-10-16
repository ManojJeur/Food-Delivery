package com.fooddelivery.order.dto.request;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
public record CartQuantityRequest(@Min(1) @Max(99) int quantity) { }
