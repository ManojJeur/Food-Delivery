package com.fooddelivery.order.exception;
public class CartRestaurantMismatchException extends RuntimeException { public CartRestaurantMismatchException() { super("A cart can contain items from only one restaurant; clear the cart first"); } }
