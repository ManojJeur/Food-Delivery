package com.fooddelivery.restaurant.exception;

public class UnauthorizedOperationException extends RuntimeException {

    public UnauthorizedOperationException() {
        super("You do not own this restaurant and cannot manage it");
    }
}
