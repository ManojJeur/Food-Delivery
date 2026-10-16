package com.fooddelivery.order.exception;
public class CartChangedException extends RuntimeException { public CartChangedException() { super("Cart changed during checkout; review it and retry"); } }
