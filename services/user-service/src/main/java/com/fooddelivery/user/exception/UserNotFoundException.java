package com.fooddelivery.user.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("User " + userId + " was not found");
    }
}
