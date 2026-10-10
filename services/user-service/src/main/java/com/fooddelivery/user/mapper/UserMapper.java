package com.fooddelivery.user.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import com.fooddelivery.user.dto.response.UserResponse;
import com.fooddelivery.user.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(AppUser user) {
        Set<String> roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toUnmodifiableSet());
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), roles,
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
