package com.fooddelivery.order.dto.response;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.fooddelivery.order.entity.OrderStatus;
public record OrderResponse(UUID id, UUID customerId, UUID restaurantId, String deliveryAddress, BigDecimal subtotal,
        BigDecimal deliveryFee, BigDecimal taxAmount, BigDecimal totalAmount, OrderStatus status,
        List<OrderItemResponse> items, List<OrderStatusHistoryResponse> statusHistory,
        OffsetDateTime createdAt, OffsetDateTime updatedAt) { }
