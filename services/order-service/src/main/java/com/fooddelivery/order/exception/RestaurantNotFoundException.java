package com.fooddelivery.order.exception;
import java.util.UUID;
public class RestaurantNotFoundException extends RuntimeException { public RestaurantNotFoundException(UUID id) { super("Restaurant " + id + " was not found"); } }
