package com.fooddelivery.menu.exception;

import java.util.UUID;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(UUID id) { super("Menu item " + id + " was not found or is unavailable"); }
}
