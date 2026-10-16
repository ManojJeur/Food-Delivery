package com.fooddelivery.order.service;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import com.fooddelivery.order.client.MenuClient;
import com.fooddelivery.order.client.MenuItemQuote;
import com.fooddelivery.order.dto.response.CartItemResponse;
import com.fooddelivery.order.dto.response.CartResponse;
import com.fooddelivery.order.entity.CartItem;
import com.fooddelivery.order.repository.CartItemRepository;
import com.fooddelivery.order.repository.CustomerCartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final CustomerCartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final CartMutationService mutations;
    private final MenuClient menuClient;
    public CartService(CustomerCartRepository carts, CartItemRepository items, CartMutationService mutations, MenuClient menuClient) {
        this.cartRepository = carts; this.itemRepository = items; this.mutations = mutations; this.menuClient = menuClient;
    }
    @Transactional(readOnly = true)
    public CartResponse get(UUID customerId) {
        var cart = cartRepository.findByCustomerId(customerId).orElse(null);
        if (cart == null) return new CartResponse(null, List.of(), BigDecimal.ZERO.setScale(2));
        List<CartItemResponse> items = itemRepository.findAllByCartIdOrderByItemNameAsc(cart.getId()).stream().map(this::map).toList();
        BigDecimal subtotal = items.stream().map(CartItemResponse::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getRestaurantId(), items, subtotal);
    }
    public CartResponse add(UUID customerId, UUID menuItemId, int quantity) {
        MenuItemQuote quote = menuClient.quote(List.of(menuItemId)).stream().filter(i -> i.id().equals(menuItemId)).findFirst()
                .orElseThrow(com.fooddelivery.order.exception.MenuItemsUnavailableException::new);
        mutations.add(customerId, quote, quantity);
        return get(customerId);
    }
    public CartResponse updateQuantity(UUID customerId, UUID menuItemId, int quantity) {
        mutations.updateQuantity(customerId, menuItemId, quantity);
        return get(customerId);
    }
    public CartResponse remove(UUID customerId, UUID menuItemId) {
        mutations.remove(customerId, menuItemId);
        return get(customerId);
    }
    public CartResponse clear(UUID customerId) {
        mutations.clear(customerId);
        return get(customerId);
    }
    private CartItemResponse map(CartItem item) {
        return new CartItemResponse(item.getMenuItemId(), item.getItemName(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal());
    }
}
