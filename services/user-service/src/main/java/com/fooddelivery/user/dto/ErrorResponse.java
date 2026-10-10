package com.fooddelivery.user.dto;

import java.time.Instant;

public record ErrorResponse(boolean success, String message, String errorCode, Instant timestamp, String path) {
}
