package com.fooddelivery.user.service;

import java.util.UUID;

import com.fooddelivery.user.dto.request.UpdateProfileRequest;
import com.fooddelivery.user.entity.AppUser;
import com.fooddelivery.user.exception.UserNotFoundException;
import com.fooddelivery.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AppUser getProfile(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public AppUser updateProfile(UUID userId, UpdateProfileRequest request) {
        AppUser user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        String phone = request.phone() == null ? user.getPhone() : request.phone().trim();
        user.updateProfile(request.fullName().trim(), phone);
        return user;
    }
}
