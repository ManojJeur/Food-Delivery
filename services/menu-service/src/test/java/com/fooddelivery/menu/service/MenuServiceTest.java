package com.fooddelivery.menu.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.fooddelivery.menu.client.RestaurantAccessClient;
import com.fooddelivery.menu.dto.request.MenuCategoryRequest;
import com.fooddelivery.menu.dto.request.MenuItemRequest;
import com.fooddelivery.menu.entity.MenuCategory;
import com.fooddelivery.menu.entity.MenuItem;
import com.fooddelivery.menu.exception.MenuCategoryNotFoundException;
import com.fooddelivery.menu.repository.MenuCategoryRepository;
import com.fooddelivery.menu.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
    @Mock private MenuCategoryRepository categoryRepository;
    @Mock private MenuItemRepository itemRepository;
    @Mock private RestaurantAccessClient restaurantAccessClient;

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new MenuService(categoryRepository, itemRepository, restaurantAccessClient);
    }

    @Test
    void createCategoryAuthorizesRestaurantBeforeSaving() {
        UUID restaurantId = UUID.randomUUID();
        when(categoryRepository.save(any(MenuCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MenuCategory category = menuService.createCategory(
                new MenuCategoryRequest(restaurantId, "  Mains  ", null, 0), "owner-token");

        assertSame(restaurantId, category.getRestaurantId());
        verify(restaurantAccessClient).assertCanManage(restaurantId, "owner-token");
        verify(categoryRepository).save(any(MenuCategory.class));
    }

    @Test
    void newMenuItemStartsUnavailableUntilOwnerEnablesIt() {
        UUID restaurantId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findByIdAndRestaurantId(categoryId, restaurantId))
                .thenReturn(Optional.of(new MenuCategory(restaurantId, "Mains", "", 0)));
        when(itemRepository.save(any(MenuItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MenuItem item = menuService.createItem(new MenuItemRequest(restaurantId, categoryId, "Noodles", "",
                new BigDecimal("8.50"), null, false, 12), "owner-token");

        assertFalse(item.isAvailable());
        verify(restaurantAccessClient).assertCanManage(restaurantId, "owner-token");
    }

    @Test
    void rejectsItemWhenCategoryBelongsToDifferentRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)).thenReturn(Optional.empty());

        assertThrows(MenuCategoryNotFoundException.class, () -> menuService.createItem(
                new MenuItemRequest(restaurantId, categoryId, "Noodles", "", new BigDecimal("8.50"),
                        null, false, 12), "owner-token"));
    }
}
