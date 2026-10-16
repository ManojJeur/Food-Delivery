package com.fooddelivery.menu.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fooddelivery.menu.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    List<MenuCategory> findAllByRestaurantIdAndActiveTrueOrderByDisplayOrderAscNameAsc(UUID restaurantId);

    List<MenuCategory> findAllByRestaurantIdOrderByDisplayOrderAscNameAsc(UUID restaurantId);

    Optional<MenuCategory> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
