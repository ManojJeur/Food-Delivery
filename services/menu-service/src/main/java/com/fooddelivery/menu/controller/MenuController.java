package com.fooddelivery.menu.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fooddelivery.menu.dto.ApiResponse;
import com.fooddelivery.menu.dto.PageResponse;
import com.fooddelivery.menu.dto.request.CategoryActiveRequest;
import com.fooddelivery.menu.dto.request.ItemAvailabilityRequest;
import com.fooddelivery.menu.dto.request.MenuCategoryRequest;
import com.fooddelivery.menu.dto.request.MenuItemRequest;
import com.fooddelivery.menu.dto.request.ItemQuoteRequest;
import com.fooddelivery.menu.dto.response.MenuCategoryResponse;
import com.fooddelivery.menu.dto.response.MenuItemResponse;
import com.fooddelivery.menu.dto.response.MenuItemQuote;
import com.fooddelivery.menu.entity.MenuCategory;
import com.fooddelivery.menu.entity.MenuItem;
import com.fooddelivery.menu.mapper.MenuMapper;
import com.fooddelivery.menu.service.MenuService;
import com.fooddelivery.menu.util.Pagination;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {
    private final MenuService menuService;
    private final MenuMapper menuMapper;

    public MenuController(MenuService menuService, MenuMapper menuMapper) {
        this.menuService = menuService;
        this.menuMapper = menuMapper;
    }

    @GetMapping("/restaurants/{restaurantId}/categories")
    public ApiResponse<List<MenuCategoryResponse>> publicCategories(@PathVariable UUID restaurantId) {
        return ApiResponse.success("Menu categories retrieved successfully",
                menuService.publicCategories(restaurantId).stream().map(menuMapper::toResponse).toList());
    }

    @GetMapping("/restaurants/{restaurantId}/management/categories")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<MenuCategoryResponse>> managementCategories(@PathVariable UUID restaurantId,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Menu categories retrieved successfully",
                menuService.managementCategories(restaurantId, token(jwt)).stream().map(menuMapper::toResponse).toList());
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MenuCategoryResponse> createCategory(@Valid @RequestBody MenuCategoryRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Menu category created successfully",
                menuMapper.toResponse(menuService.createCategory(request, token(jwt))));
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MenuCategoryResponse> updateCategory(@PathVariable UUID categoryId,
            @Valid @RequestBody MenuCategoryRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Menu category updated successfully",
                menuMapper.toResponse(menuService.updateCategory(categoryId, request, token(jwt))));
    }

    @PatchMapping("/categories/{categoryId}/active")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MenuCategoryResponse> setCategoryActive(@PathVariable UUID categoryId,
            @Valid @RequestBody CategoryActiveRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Menu category visibility updated successfully",
                menuMapper.toResponse(menuService.changeCategoryActive(categoryId, request.active(), token(jwt))));
    }

    @GetMapping("/restaurants/{restaurantId}/items")
    public ApiResponse<PageResponse<MenuItemResponse>> publicItems(@PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "") String keyword, @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal minPrice,
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
        Pageable pageable = Pagination.of(page, size, sort);
        Page<MenuItemResponse> results = menuService.searchPublicItems(restaurantId, keyword, categoryId, vegetarian,
                minPrice, maxPrice, pageable).map(menuMapper::toResponse);
        return ApiResponse.success("Available menu items retrieved successfully", PageResponse.from(results));
    }

    @GetMapping("/restaurants/{restaurantId}/management/items")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PageResponse<MenuItemResponse>> managementItems(@PathVariable UUID restaurantId,
            @RequestParam(defaultValue = "") String keyword, @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort, @AuthenticationPrincipal Jwt jwt) {
        Pageable pageable = Pagination.of(page, size, sort);
        Page<MenuItemResponse> results = menuService.searchManagementItems(restaurantId, keyword, categoryId,
                vegetarian, pageable, token(jwt)).map(menuMapper::toResponse);
        return ApiResponse.success("Menu items retrieved successfully", PageResponse.from(results));
    }

    @GetMapping("/items/{itemId}")
    public ApiResponse<MenuItemResponse> getItem(@PathVariable UUID itemId) {
        return ApiResponse.success("Menu item retrieved successfully",
                menuMapper.toResponse(menuService.getPublicItem(itemId)));
    }

    @PostMapping("/items/quote")
    public ApiResponse<List<MenuItemQuote>> quoteAvailableItems(@Valid @RequestBody ItemQuoteRequest request) {
        return ApiResponse.success("Current menu prices retrieved successfully",
                menuService.quoteAvailableItems(request.itemIds()));
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MenuItemResponse> createItem(@Valid @RequestBody MenuItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Menu item created successfully",
                menuMapper.toResponse(menuService.createItem(request, token(jwt))));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MenuItemResponse> updateItem(@PathVariable UUID itemId,
            @Valid @RequestBody MenuItemRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Menu item updated successfully",
                menuMapper.toResponse(menuService.updateItem(itemId, request, token(jwt))));
    }

    @PatchMapping("/items/{itemId}/availability")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MenuItemResponse> setItemAvailability(@PathVariable UUID itemId,
            @Valid @RequestBody ItemAvailabilityRequest request, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Menu item availability updated successfully", menuMapper.toResponse(
                menuService.changeItemAvailability(itemId, request.available(), token(jwt))));
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public void deleteItem(@PathVariable UUID itemId, @AuthenticationPrincipal Jwt jwt) {
        menuService.deleteItem(itemId, token(jwt));
    }

    private String token(Jwt jwt) { return jwt.getTokenValue(); }
}
