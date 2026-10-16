package com.fooddelivery.order.dto;
import java.util.List;
import java.util.UUID;
public record CartSnapshot(UUID restaurantId, long version, List<CartLineSnapshot> items) { }
