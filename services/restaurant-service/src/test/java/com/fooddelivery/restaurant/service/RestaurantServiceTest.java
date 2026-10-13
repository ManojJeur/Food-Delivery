package com.fooddelivery.restaurant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import com.fooddelivery.restaurant.dto.request.RestaurantRequest;
import com.fooddelivery.restaurant.dto.request.RestaurantStatusRequest;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.entity.RestaurantStatus;
import com.fooddelivery.restaurant.exception.InvalidRestaurantStatusException;
import com.fooddelivery.restaurant.exception.UnauthorizedOperationException;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    @Test
    void creationSetsAuthenticatedOwnerAndStartsClosed() {
        UUID ownerId = UUID.randomUUID();
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Restaurant restaurant = restaurantService.create(ownerId, validRequest());

        assertEquals(ownerId, restaurant.getOwnerId());
        assertEquals(RestaurantStatus.CLOSED, restaurant.getStatus());
        assertEquals("Example Kitchen", restaurant.getName());
    }

    @Test
    void ownerCannotUpdateAnotherOwnersRestaurant() {
        UUID ownerId = UUID.randomUUID();
        UUID otherOwnerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(
                new Restaurant(otherOwnerId, "Other Kitchen", "Description", "+919876543210",
                        "owner@example.com", "Address", BigDecimal.ZERO, BigDecimal.ZERO,
                        LocalTime.of(9, 0), LocalTime.of(21, 0))));

        assertThrows(UnauthorizedOperationException.class,
                () -> restaurantService.update(ownerId, false, restaurantId, validRequest()));
    }

    @Test
    void restaurantOwnerCannotSetSuspendedStatus() {
        UUID ownerId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(ownerId, "Example Kitchen", "Description", "+919876543210",
                "owner@example.com", "Address", BigDecimal.ZERO, BigDecimal.ZERO,
                LocalTime.of(9, 0), LocalTime.of(21, 0));
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        assertThrows(InvalidRestaurantStatusException.class,
                () -> restaurantService.updateStatus(ownerId, false, restaurantId,
                        new RestaurantStatusRequest(RestaurantStatus.SUSPENDED)));
    }

    private RestaurantRequest validRequest() {
        return new RestaurantRequest(" Example Kitchen ", " Neighborhood restaurant ", "+919876543210",
                "OWNER@EXAMPLE.COM", " 42 Sample Road ", new BigDecimal("12.9716000"),
                new BigDecimal("77.5946000"), LocalTime.of(9, 0), LocalTime.of(21, 0));
    }
}
