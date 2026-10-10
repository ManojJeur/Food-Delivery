package com.fooddelivery.user.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AuthResponse(
        String tokenType,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UUID userId,
        String email,
        Set<String> roles) {
}
