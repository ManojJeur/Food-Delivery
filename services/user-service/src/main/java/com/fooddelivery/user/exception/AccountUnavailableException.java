package com.fooddelivery.user.exception;

public class AccountUnavailableException extends RuntimeException {

    public AccountUnavailableException() {
        super("This account is not active");
    }
}
