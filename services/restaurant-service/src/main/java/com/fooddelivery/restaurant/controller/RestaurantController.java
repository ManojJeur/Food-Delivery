package com.fooddelivery.restaurant.controller;

import java.util.List;
import java.util.UUID;

import com.fooddelivery.restaurant.dto.ApiResponse;
import com.fooddelivery.restaurant.dto.PageResponse;
import com.fooddelivery.restaurant.dto.request.RestaurantRequest;
import com.fooddelivery.restaurant.dto.request.RestaurantStatusRequest;
import com.fooddelivery.restaurant.dto.response.RestaurantResponse;
import com.fooddelivery.restaurant.entity.Restaurant;
import com.fooddelivery.restaurant.mapper.RestaurantMapper;
import com.fooddelivery.restaurant.service.RestaurantService;
import com.fooddelivery.restaurant.util.Pagination;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantMapper restaurantMapper;

    public RestaurantController(RestaurantService restaurantService, RestaurantMapper restaurantMapper) {
        this.restaurantService = restaurantService;
        this.restaurantMapper = restaurantMapper;
    }

    @GetMapping
    public ApiResponse<PageResponse<RestaurantResponse>> discover(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
        Pageable pageable = Pagination.of(page, size, sort);
        Page<RestaurantResponse> content = restaurantService.discover(keyword, pageable).map(restaurantMapper::toResponse);
        return ApiResponse.success("Restaurants retrieved successfully", PageResponse.from(content));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<RestaurantResponse>> listOwned(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Pageable pageable = Pagination.of(page, size, sort);
        Page<RestaurantResponse> content = restaurantService.listOwned(userId(jwt), pageable).map(restaurantMapper::toResponse);
        return ApiResponse.success("Your restaurants retrieved successfully", PageResponse.from(content));
    }

    @GetMapping("/{restaurantId}")
    public ApiResponse<RestaurantResponse> getById(@PathVariable UUID restaurantId) {
        return ApiResponse.success("Restaurant retrieved successfully",
                restaurantMapper.toResponse(restaurantService.getPublicRestaurant(restaurantId)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<RestaurantResponse> create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RestaurantRequest request) {
        return ApiResponse.success("Restaurant created successfully",
                restaurantMapper.toResponse(restaurantService.create(userId(jwt), request)));
    }

    @PutMapping("/{restaurantId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<RestaurantResponse> update(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID restaurantId, @Valid @RequestBody RestaurantRequest request) {
        return ApiResponse.success("Restaurant updated successfully", restaurantMapper.toResponse(
                restaurantService.update(userId(jwt), isAdministrator(jwt), restaurantId, request)));
    }

    @PatchMapping("/{restaurantId}/status")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<RestaurantResponse> updateStatus(@AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID restaurantId, @Valid @RequestBody RestaurantStatusRequest request) {
        return ApiResponse.success("Restaurant status updated successfully", restaurantMapper.toResponse(
                restaurantService.updateStatus(userId(jwt), isAdministrator(jwt), restaurantId, request)));
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private boolean isAdministrator(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains("ADMIN");
    }
}
