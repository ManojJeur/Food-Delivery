package com.fooddelivery.user.dto.request;

import com.fooddelivery.user.entity.AddressLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotNull AddressLabel label,
        @NotBlank @Size(max = 120) String recipientName,
        @NotBlank @Pattern(regexp = "^[+0-9() .-]{7,30}$") String phone,
        @NotBlank @Size(max = 200) String line1,
        @Size(max = 200) String line2,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Pattern(regexp = "^[A-Z]{2}$") String countryCode) {
}
