package com.fooddelivery.restaurant.repository;

import java.util.Optional;
import java.util.UUID;

import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.entity.RestaurantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Page<Restaurant> findByNameContainingIgnoreCaseAndStatusNot(String name, RestaurantStatus excludedStatus,
            Pageable pageable);

    Page<Restaurant> findAllByOwnerId(UUID ownerId, Pageable pageable);

    Optional<Restaurant> findByIdAndStatusNot(UUID id, RestaurantStatus excludedStatus);
}
