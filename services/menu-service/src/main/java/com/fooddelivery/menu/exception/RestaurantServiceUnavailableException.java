package com.fooddelivery.menu.exception;

public class RestaurantServiceUnavailableException extends RuntimeException {
    public RestaurantServiceUnavailableException() {
        super("Restaurant Service is temporarily unavailable; menu changes cannot be authorized");
    }
}
