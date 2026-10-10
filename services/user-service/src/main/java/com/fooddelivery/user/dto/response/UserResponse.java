package com.fooddelivery.user.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(UUID id, String fullName, String email, String phone, Set<String> roles,
        Instant createdAt, Instant updatedAt) {
}
