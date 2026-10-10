package com.fooddelivery.user.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.fooddelivery.user.entity.AddressLabel;

public record AddressResponse(UUID id, AddressLabel label, String recipientName, String phone, String line1,
        String line2, String city, String state, String postalCode, String countryCode,
        Instant createdAt, Instant updatedAt) {
}
