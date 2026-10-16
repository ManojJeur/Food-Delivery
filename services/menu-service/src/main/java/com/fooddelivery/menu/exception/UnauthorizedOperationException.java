package com.fooddelivery.menu.exception;

public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException() { super("You are not allowed to manage this restaurant menu"); }
}
