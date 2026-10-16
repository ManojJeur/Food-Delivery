package com.fooddelivery.order.exception;
public class InvalidOrderTransitionException extends RuntimeException { public InvalidOrderTransitionException() { super("This order status transition is not allowed"); } }
