package com.fooddelivery.menu.exception;

public class MenuItemUnavailableException extends RuntimeException {
    public MenuItemUnavailableException() {
        super("One or more menu items are missing or unavailable");
    }
}
