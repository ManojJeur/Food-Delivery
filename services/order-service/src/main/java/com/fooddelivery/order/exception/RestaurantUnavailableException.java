package com.fooddelivery.order.exception;
public class RestaurantUnavailableException extends RuntimeException { public RestaurantUnavailableException() { super("Restaurant is not currently open for orders"); } }
