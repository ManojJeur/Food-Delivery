package com.fooddelivery.user.exception;

import java.util.UUID;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(UUID addressId) {
        super("Address " + addressId + " was not found");
    }
}
