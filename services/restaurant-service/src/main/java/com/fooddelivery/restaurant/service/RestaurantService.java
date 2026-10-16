package com.fooddelivery.restaurant.service;

import java.time.LocalTime;
import java.util.UUID;

import com.fooddelivery.restaurant.dto.request.RestaurantRequest;
import com.fooddelivery.restaurant.dto.request.RestaurantStatusRequest;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.entity.RestaurantStatus;
import com.fooddelivery.restaurant.exception.InvalidRestaurantStatusException;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.exception.UnauthorizedOperationException;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public Restaurant create(UUID ownerId, RestaurantRequest request) {
        ensureDifferentOperatingTimes(request.openingTime(), request.closingTime());
        Restaurant restaurant = new Restaurant(ownerId, clean(request.name()), clean(request.description()),
                clean(request.phone()), clean(request.email()).toLowerCase(java.util.Locale.ROOT),
                clean(request.address()), request.latitude(), request.longitude(), request.openingTime(),
                request.closingTime());
        return restaurantRepository.save(restaurant);
    }

    @Transactional(readOnly = true)
    public Restaurant getPublicRestaurant(UUID restaurantId) {
        return restaurantRepository.findByIdAndStatusNot(restaurantId, RestaurantStatus.SUSPENDED)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
    }

    @Transactional(readOnly = true)
    public void assertCanManage(UUID currentUserId, boolean administrator, UUID restaurantId) {
        findManaged(currentUserId, administrator, restaurantId);
    }

    @Transactional(readOnly = true)
    public Page<Restaurant> discover(String keyword, Pageable pageable) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        return restaurantRepository.findByNameContainingIgnoreCaseAndStatusNot(safeKeyword,
                RestaurantStatus.SUSPENDED, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Restaurant> listOwned(UUID ownerId, Pageable pageable) {
        return restaurantRepository.findAllByOwnerId(ownerId, pageable);
    }

    @Transactional
    public Restaurant update(UUID currentUserId, boolean administrator, UUID restaurantId,
            RestaurantRequest request) {
        Restaurant restaurant = findManaged(currentUserId, administrator, restaurantId);
        ensureDifferentOperatingTimes(request.openingTime(), request.closingTime());
        restaurant.updateDetails(clean(request.name()), clean(request.description()), clean(request.phone()),
                clean(request.email()).toLowerCase(java.util.Locale.ROOT), clean(request.address()), request.latitude(),
                request.longitude(), request.openingTime(), request.closingTime());
        return restaurant;
    }

    @Transactional
    public Restaurant updateStatus(UUID currentUserId, boolean administrator, UUID restaurantId,
            RestaurantStatusRequest request) {
        Restaurant restaurant = findManaged(currentUserId, administrator, restaurantId);
        if (!administrator && request.status() == RestaurantStatus.SUSPENDED) {
            throw new InvalidRestaurantStatusException();
        }
        restaurant.changeStatus(request.status());
        return restaurant;
    }

    private Restaurant findManaged(UUID currentUserId, boolean administrator, UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        if (!administrator && !restaurant.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedOperationException();
        }
        return restaurant;
    }

    private void ensureDifferentOperatingTimes(LocalTime openingTime, LocalTime closingTime) {
        if (openingTime.equals(closingTime)) {
            throw new IllegalArgumentException("Opening time and closing time must be different");
        }
    }

    private String clean(String value) {
        return value.trim();
    }
}
