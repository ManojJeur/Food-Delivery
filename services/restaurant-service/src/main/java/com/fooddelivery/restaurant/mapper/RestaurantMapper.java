package com.fooddelivery.restaurant.mapper;

import com.fooddelivery.restaurant.dto.response.RestaurantResponse;
import com.fooddelivery.restaurant.entity.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper {

    public RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getDescription(),
                restaurant.getPhone(), restaurant.getEmail(), restaurant.getAddress(), restaurant.getLatitude(),
                restaurant.getLongitude(), restaurant.getStatus(), restaurant.getOpeningTime(),
                restaurant.getClosingTime(), restaurant.getCreatedAt(), restaurant.getUpdatedAt());
    }
}
