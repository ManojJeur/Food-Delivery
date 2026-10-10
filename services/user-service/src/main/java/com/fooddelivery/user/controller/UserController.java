package com.fooddelivery.user.controller;

import java.util.UUID;

import com.fooddelivery.user.dto.ApiResponse;
import com.fooddelivery.user.dto.request.UpdateProfileRequest;
import com.fooddelivery.user.dto.response.UserResponse;
import com.fooddelivery.user.mapper.UserMapper;
import com.fooddelivery.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserProfileService userProfileService;
    private final UserMapper userMapper;

    public UserController(UserProfileService userProfileService, UserMapper userMapper) {
        this.userProfileService = userProfileService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<UserResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success("Profile retrieved successfully",
                userMapper.toResponse(userProfileService.getProfile(userId)));
    }

    @PatchMapping
    public ApiResponse<UserResponse> updateProfile(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success("Profile updated successfully",
                userMapper.toResponse(userProfileService.updateProfile(userId, request)));
    }
}
