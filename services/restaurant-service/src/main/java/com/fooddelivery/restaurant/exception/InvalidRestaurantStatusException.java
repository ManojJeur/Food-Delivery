package com.fooddelivery.restaurant.exception;

public class InvalidRestaurantStatusException extends RuntimeException {

    public InvalidRestaurantStatusException() {
        super("Restaurant owners cannot set the restaurant status to SUSPENDED");
    }
}
