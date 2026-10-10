package com.fooddelivery.user.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;

import com.fooddelivery.user.dto.request.LoginRequest;
import com.fooddelivery.user.dto.request.RegisterRequest;
import com.fooddelivery.user.dto.response.AuthResponse;
import com.fooddelivery.user.entity.AppUser;
import com.fooddelivery.user.entity.RefreshToken;
import com.fooddelivery.user.exception.AccountUnavailableException;
import com.fooddelivery.user.exception.DuplicateEmailException;
import com.fooddelivery.user.exception.InvalidCredentialsException;
import com.fooddelivery.user.exception.InvalidRefreshTokenException;
import com.fooddelivery.user.repository.RefreshTokenRepository;
import com.fooddelivery.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        ensureBcryptCompatible(request.password());

        AppUser user = new AppUser(request.fullName().trim(), email, passwordEncoder.encode(request.password()));
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
        return tokenService.issueFor(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        AppUser user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        ensureBcryptCompatible(request.password());
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (!user.isActive()) {
            throw new AccountUnavailableException();
        }
        return tokenService.issueFor(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String tokenHash = tokenService.hashToken(rawRefreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);
        Instant now = Instant.now();
        if (!storedToken.isUsableAt(now)) {
            throw new InvalidRefreshTokenException();
        }

        AppUser user = storedToken.getUser();
        if (!user.isActive()) {
            storedToken.revoke(now);
            throw new AccountUnavailableException();
        }

        storedToken.revoke(now);
        return tokenService.issueFor(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String tokenHash = tokenService.hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .ifPresent(token -> token.revoke(Instant.now()));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureBcryptCompatible(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be no more than 72 UTF-8 bytes");
        }
    }
}
