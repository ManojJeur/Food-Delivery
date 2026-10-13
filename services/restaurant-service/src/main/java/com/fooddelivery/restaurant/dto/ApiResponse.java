package com.fooddelivery.restaurant.dto;

public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
}
