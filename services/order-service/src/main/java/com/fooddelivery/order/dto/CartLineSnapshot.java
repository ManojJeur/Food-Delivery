package com.fooddelivery.order.dto;
import java.util.UUID;
public record CartLineSnapshot(UUID menuItemId, int quantity) { }
