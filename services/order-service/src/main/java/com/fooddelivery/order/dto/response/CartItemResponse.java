package com.fooddelivery.order.dto.response;
import java.math.BigDecimal;
import java.util.UUID;
public record CartItemResponse(UUID menuItemId, String itemName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) { }
