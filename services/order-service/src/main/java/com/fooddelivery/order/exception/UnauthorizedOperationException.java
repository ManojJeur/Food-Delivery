package com.fooddelivery.order.exception;
public class UnauthorizedOperationException extends RuntimeException { public UnauthorizedOperationException() { super("You are not allowed to access or change this order"); } }
