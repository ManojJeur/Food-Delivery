package com.fooddelivery.order.exception;
public class IdempotencyKeyConflictException extends RuntimeException { public IdempotencyKeyConflictException() { super("Idempotency-Key was already used with a different delivery address"); } }
