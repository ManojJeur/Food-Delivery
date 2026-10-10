package com.fooddelivery.user.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import java.util.stream.Collectors;

import com.fooddelivery.user.dto.response.AuthResponse;
import com.fooddelivery.user.entity.AppUser;
import com.fooddelivery.user.entity.RefreshToken;
import com.fooddelivery.user.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RAW_REFRESH_TOKEN_BYTES = 48;

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public TokenService(JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokenRepository,
            @Value("${security.jwt.access-token-ttl}") Duration accessTokenTtl,
            @Value("${security.jwt.refresh-token-ttl}") Duration refreshTokenTtl) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public AuthResponse issueFor(AppUser user) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plus(accessTokenTtl);
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                        .issuer("food-delivery-user-service")
                        .issuedAt(now)
                        .expiresAt(accessExpiresAt)
                        .subject(user.getId().toString())
                        .claim("email", user.getEmail())
                        .claim("roles", user.getRoles().stream().map(Enum::name).sorted().toList())
                        .build()))
                .getTokenValue();

        String refreshToken = createOpaqueToken();
        Instant refreshExpiresAt = now.plus(refreshTokenTtl);
        refreshTokenRepository.save(new RefreshToken(user, hashToken(refreshToken), refreshExpiresAt));

        Set<String> roleNames = user.getRoles().stream().map(Enum::name).collect(Collectors.toUnmodifiableSet());
        return new AuthResponse("Bearer", accessToken, accessExpiresAt, refreshToken, refreshExpiresAt,
                user.getId(), user.getEmail(), roleNames);
    }

    public String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String createOpaqueToken() {
        byte[] bytes = new byte[RAW_REFRESH_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
