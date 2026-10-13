package com.fooddelivery.restaurant.dto.request;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RestaurantRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 5000) String description,
        @NotBlank @Pattern(regexp = "^[+0-9() .-]{7,30}$") String phone,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 300) String address,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @NotNull LocalTime openingTime,
        @NotNull LocalTime closingTime) {

    @AssertTrue(message = "opening time and closing time must be different")
    public boolean isOperatingHoursValid() {
        return openingTime == null || closingTime == null || !openingTime.equals(closingTime);
    }
}
