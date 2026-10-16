package com.fooddelivery.order.dto;
import java.time.Instant;
public record ErrorResponse(boolean success, String message, String errorCode, Instant timestamp, String path) { }
