package com.fooddelivery.order.dto.response;
import java.math.BigDecimal;
import java.util.UUID;
public record OrderItemResponse(UUID menuItemId, String itemName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal, boolean vegetarian) { }
