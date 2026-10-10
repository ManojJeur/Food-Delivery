package com.fooddelivery.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddelivery.user.entity.UserAddress;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<UserAddress> findByIdAndUserId(UUID addressId, UUID userId);
}
