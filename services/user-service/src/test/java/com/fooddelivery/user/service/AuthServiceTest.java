package com.fooddelivery.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fooddelivery.user.dto.request.LoginRequest;
import com.fooddelivery.user.dto.request.RegisterRequest;
import com.fooddelivery.user.dto.response.AuthResponse;
import com.fooddelivery.user.entity.AppUser;
import com.fooddelivery.user.exception.DuplicateEmailException;
import com.fooddelivery.user.exception.InvalidCredentialsException;
import com.fooddelivery.user.repository.RefreshTokenRepository;
import com.fooddelivery.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authResponse = new AuthResponse("Bearer", "access", Instant.now().plusSeconds(900),
                "refresh", Instant.now().plusSeconds(86400), UUID.randomUUID(), "user@example.com", Set.of("CUSTOMER"));
    }

    @Test
    void registerNormalizesEmailAndOnlyAssignsCustomerRole() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("a-long-password-123")).thenReturn("bcrypt-hash");
        when(userRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.issueFor(any(AppUser.class))).thenReturn(authResponse);

        AuthResponse result = authService.register(new RegisterRequest("  Alex Example ", "USER@Example.com",
                "a-long-password-123", null));

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        assertEquals("user@example.com", userCaptor.getValue().getEmail());
        assertEquals("Alex Example", userCaptor.getValue().getFullName());
        assertEquals(Set.of(com.fooddelivery.user.entity.Role.CUSTOMER), userCaptor.getValue().getRoles());
        assertEquals(authResponse, result);
    }

    @Test
    void registerRejectsExistingEmailBeforeSaving() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(
                new RegisterRequest("Alex Example", "user@example.com", "a-long-password-123", null)));

        verify(userRepository, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void loginReturnsSameAuthenticationErrorForWrongPassword() {
        AppUser user = new AppUser("Alex Example", "user@example.com", "bcrypt-hash");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password-123", "bcrypt-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("USER@example.com", "wrong-password-123")));

        verify(tokenService, never()).issueFor(any(AppUser.class));
    }
}
