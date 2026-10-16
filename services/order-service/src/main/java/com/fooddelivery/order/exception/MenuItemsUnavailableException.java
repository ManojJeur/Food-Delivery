package com.fooddelivery.order.exception;
public class MenuItemsUnavailableException extends RuntimeException { public MenuItemsUnavailableException() { super("One or more cart items are unavailable; refresh the cart and try again"); } }
