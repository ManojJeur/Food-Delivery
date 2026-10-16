package com.fooddelivery.order.dto.response;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record CartResponse(UUID restaurantId, List<CartItemResponse> items, BigDecimal subtotal) { }
