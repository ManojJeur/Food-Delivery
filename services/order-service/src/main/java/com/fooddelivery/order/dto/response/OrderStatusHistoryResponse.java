package com.fooddelivery.order.dto.response;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fooddelivery.order.entity.OrderStatus;
public record OrderStatusHistoryResponse(OrderStatus fromStatus, OrderStatus toStatus, UUID changedBy, OffsetDateTime changedAt) { }
