package com.fooddelivery.menu.exception;

import java.util.UUID;

public class MenuCategoryNotFoundException extends RuntimeException {
    public MenuCategoryNotFoundException(UUID id) { super("Menu category " + id + " was not found"); }
}
