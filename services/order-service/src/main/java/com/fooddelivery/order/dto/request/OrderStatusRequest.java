package com.fooddelivery.order.dto.request;
import com.fooddelivery.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
public record OrderStatusRequest(@NotNull OrderStatus status) { }
