package com.fooddelivery.menu.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import com.fooddelivery.menu.client.RestaurantAccessClient;
import com.fooddelivery.menu.dto.request.MenuCategoryRequest;
import com.fooddelivery.menu.dto.request.MenuItemRequest;
import com.fooddelivery.menu.dto.response.MenuItemQuote;
import com.fooddelivery.menu.entity.MenuCategory;
import com.fooddelivery.menu.entity.MenuItem;
import com.fooddelivery.menu.exception.MenuCategoryNotFoundException;
import com.fooddelivery.menu.exception.MenuItemNotFoundException;
import com.fooddelivery.menu.exception.MenuItemUnavailableException;
import com.fooddelivery.menu.repository.MenuCategoryRepository;
import com.fooddelivery.menu.repository.MenuItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final RestaurantAccessClient restaurantAccessClient;

    public MenuService(MenuCategoryRepository categoryRepository, MenuItemRepository itemRepository,
            RestaurantAccessClient restaurantAccessClient) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.restaurantAccessClient = restaurantAccessClient;
    }

    @Transactional(readOnly = true)
    public List<MenuCategory> publicCategories(UUID restaurantId) {
        return categoryRepository.findAllByRestaurantIdAndActiveTrueOrderByDisplayOrderAscNameAsc(restaurantId);
    }

    @Transactional(readOnly = true)
    public List<MenuCategory> managementCategories(UUID restaurantId, String token) {
        authorize(restaurantId, token);
        return categoryRepository.findAllByRestaurantIdOrderByDisplayOrderAscNameAsc(restaurantId);
    }

    @Transactional
    public MenuCategory createCategory(MenuCategoryRequest request, String token) {
        authorize(request.restaurantId(), token);
        return categoryRepository.save(new MenuCategory(request.restaurantId(), clean(request.name()),
                cleanOptional(request.description()), request.displayOrder()));
    }

    @Transactional
    public MenuCategory updateCategory(UUID categoryId, MenuCategoryRequest request, String token) {
        MenuCategory category = findCategory(categoryId);
        requireSameRestaurant(category.getRestaurantId(), request.restaurantId());
        authorize(category.getRestaurantId(), token);
        category.update(clean(request.name()), cleanOptional(request.description()), request.displayOrder());
        return category;
    }

    @Transactional
    public MenuCategory changeCategoryActive(UUID categoryId, boolean active, String token) {
        MenuCategory category = findCategory(categoryId);
        authorize(category.getRestaurantId(), token);
        category.changeActive(active);
        return category;
    }

    @Transactional(readOnly = true)
    public Page<MenuItem> searchPublicItems(UUID restaurantId, String keyword, UUID categoryId,
            Boolean vegetarian, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        validatePriceRange(minPrice, maxPrice);
        return itemRepository.searchAvailable(restaurantId, normalizeKeyword(keyword), categoryId,
                vegetarian, minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MenuItem> searchManagementItems(UUID restaurantId, String keyword, UUID categoryId,
            Boolean vegetarian, Pageable pageable, String token) {
        authorize(restaurantId, token);
        return itemRepository.searchManagement(restaurantId, normalizeKeyword(keyword), categoryId,
                vegetarian, pageable);
    }

    @Transactional(readOnly = true)
    public MenuItem getPublicItem(UUID itemId) {
        return itemRepository.findPublicById(itemId).orElseThrow(() -> new MenuItemNotFoundException(itemId));
    }

    @Transactional(readOnly = true)
    public List<MenuItemQuote> quoteAvailableItems(List<UUID> requestedIds) {
        List<UUID> itemIds = requestedIds.stream().distinct().toList();
        List<MenuItem> items = itemRepository.findAllAvailableByIdIn(itemIds);
        if (items.size() != itemIds.size()) throw new MenuItemUnavailableException();
        return items.stream().map(item -> new MenuItemQuote(item.getId(), item.getRestaurantId(), item.getName(),
                item.getPrice(), item.isVegetarian())).toList();
    }

    @Transactional
    public MenuItem createItem(MenuItemRequest request, String token) {
        authorize(request.restaurantId(), token);
        requireActiveCategory(request.categoryId(), request.restaurantId());
        return itemRepository.save(new MenuItem(request.restaurantId(), request.categoryId(), clean(request.name()),
                cleanOptional(request.description()), request.price(), cleanNullable(request.imageUrl()),
                request.vegetarian(), request.preparationTimeMinutes()));
    }

    @Transactional
    public MenuItem updateItem(UUID itemId, MenuItemRequest request, String token) {
        MenuItem item = findItem(itemId);
        requireSameRestaurant(item.getRestaurantId(), request.restaurantId());
        authorize(item.getRestaurantId(), token);
        requireActiveCategory(request.categoryId(), item.getRestaurantId());
        item.update(request.categoryId(), clean(request.name()), cleanOptional(request.description()),
                request.price(), cleanNullable(request.imageUrl()), request.vegetarian(),
                request.preparationTimeMinutes());
        return item;
    }

    @Transactional
    public MenuItem changeItemAvailability(UUID itemId, boolean available, String token) {
        MenuItem item = findItem(itemId);
        authorize(item.getRestaurantId(), token);
        if (available) requireActiveCategory(item.getCategoryId(), item.getRestaurantId());
        item.changeAvailability(available);
        return item;
    }

    @Transactional
    public void deleteItem(UUID itemId, String token) {
        MenuItem item = findItem(itemId);
        authorize(item.getRestaurantId(), token);
        item.deactivate();
    }

    private MenuCategory findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new MenuCategoryNotFoundException(categoryId));
    }
    private MenuCategory requireActiveCategory(UUID categoryId, UUID restaurantId) {
        MenuCategory category = categoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new MenuCategoryNotFoundException(categoryId));
        if (!category.isActive()) throw new IllegalArgumentException("Menu items require an active category");
        return category;
    }
    private MenuItem findItem(UUID itemId) {
        MenuItem item = itemRepository.findById(itemId).orElseThrow(() -> new MenuItemNotFoundException(itemId));
        if (!item.isActive()) throw new MenuItemNotFoundException(itemId);
        return item;
    }
    private void authorize(UUID restaurantId, String token) { restaurantAccessClient.assertCanManage(restaurantId, token); }
    private void requireSameRestaurant(UUID existing, UUID requested) {
        if (!existing.equals(requested)) throw new IllegalArgumentException("A menu resource cannot be moved to another restaurant");
    }
    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
    }
    private String normalizeKeyword(String keyword) { return keyword == null ? "" : keyword.trim(); }
    private String clean(String value) { return value.trim(); }
    private String cleanOptional(String value) { return value == null ? "" : value.trim(); }
    private String cleanNullable(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
