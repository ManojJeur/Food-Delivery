package com.fooddelivery.menu.mapper;

import com.fooddelivery.menu.dto.response.MenuCategoryResponse;
import com.fooddelivery.menu.dto.response.MenuItemResponse;
import com.fooddelivery.menu.entity.MenuCategory;
import com.fooddelivery.menu.entity.MenuItem;
import org.springframework.stereotype.Component;

@Component
public class MenuMapper {

    public MenuCategoryResponse toResponse(MenuCategory category) {
        return new MenuCategoryResponse(category.getId(), category.getRestaurantId(), category.getName(),
                category.getDescription(), category.getDisplayOrder(), category.isActive(), category.getCreatedAt(),
                category.getUpdatedAt());
    }

    public MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(item.getId(), item.getRestaurantId(), item.getCategoryId(), item.getName(),
                item.getDescription(), item.getPrice(), item.getImageUrl(), item.isVegetarian(), item.isAvailable(),
                item.getPreparationTimeMinutes(), item.isActive(), item.getCreatedAt(), item.getUpdatedAt());
    }
}
