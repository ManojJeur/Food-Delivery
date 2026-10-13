package com.fooddelivery.restaurant.exception;

import java.util.UUID;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(UUID restaurantId) {
        super("Restaurant " + restaurantId + " was not found");
    }
}
