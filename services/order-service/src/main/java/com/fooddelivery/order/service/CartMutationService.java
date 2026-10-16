package com.fooddelivery.order.service;
import java.util.List;
import java.util.UUID;
import com.fooddelivery.order.client.MenuItemQuote;
import com.fooddelivery.order.dto.CartLineSnapshot;
import com.fooddelivery.order.dto.CartSnapshot;
import com.fooddelivery.order.entity.CartItem;
import com.fooddelivery.order.entity.CustomerCart;
import com.fooddelivery.order.exception.CartNotFoundException;
import com.fooddelivery.order.exception.CartRestaurantMismatchException;
import com.fooddelivery.order.exception.EmptyCartException;
import com.fooddelivery.order.repository.CartItemRepository;
import com.fooddelivery.order.repository.CustomerCartRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartMutationService {
    private final CustomerCartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final EntityManager entityManager;
    public CartMutationService(CustomerCartRepository carts, CartItemRepository items, EntityManager entityManager) {
        this.cartRepository = carts; this.itemRepository = items; this.entityManager = entityManager;
    }
    @Transactional
    public void add(UUID customerId, MenuItemQuote quote, int quantity) {
        CustomerCart cart = lockOrCreate(customerId);
        if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(quote.restaurantId()))
            throw new CartRestaurantMismatchException();
        cart.setRestaurantId(quote.restaurantId());
        CartItem item = itemRepository.findForUpdate(cart.getId(), quote.id()).orElse(null);
        if (item == null) {
            if (itemRepository.countByCartId(cart.getId()) >= 100) throw new IllegalArgumentException("A cart can contain at most 100 distinct items");
            itemRepository.save(new CartItem(cart.getId(), quote.id(), quote.name(), quote.price(), quantity));
        } else {
            int nextQuantity = item.getQuantity() + quantity;
            if (nextQuantity > 99) throw new IllegalArgumentException("Cart item quantity cannot exceed 99");
            item.update(quote.name(), quote.price(), nextQuantity);
        }
        cart.touch();
        cartRepository.save(cart);
    }
    @Transactional
    public void updateQuantity(UUID customerId, UUID menuItemId, int quantity) {
        CustomerCart cart = cartRepository.findByCustomerIdForUpdate(customerId).orElseThrow(CartNotFoundException::new);
        CartItem item = itemRepository.findForUpdate(cart.getId(), menuItemId).orElseThrow(CartNotFoundException::new);
        item.update(item.getItemName(), item.getUnitPrice(), quantity);
        cart.touch(); cartRepository.save(cart);
    }
    @Transactional
    public void remove(UUID customerId, UUID menuItemId) {
        CustomerCart cart = cartRepository.findByCustomerIdForUpdate(customerId).orElseThrow(CartNotFoundException::new);
        CartItem item = itemRepository.findForUpdate(cart.getId(), menuItemId).orElseThrow(CartNotFoundException::new);
        itemRepository.delete(item);
        if (itemRepository.countByCartId(cart.getId()) == 0) cart.clearRestaurant();
        cart.touch(); cartRepository.save(cart);
    }
    @Transactional
    public void clear(UUID customerId) {
        CustomerCart cart = cartRepository.findByCustomerIdForUpdate(customerId).orElse(null);
        if (cart == null) return;
        itemRepository.deleteAllByCartId(cart.getId()); cart.clearRestaurant(); cart.touch(); cartRepository.save(cart);
    }
    @Transactional(readOnly = true)
    public CartSnapshot snapshotForCheckout(UUID customerId) {
        CustomerCart cart = cartRepository.findByCustomerId(customerId).orElseThrow(EmptyCartException::new);
        List<CartLineSnapshot> lines = itemRepository.findAllByCartIdOrderByItemNameAsc(cart.getId()).stream()
                .map(i -> new CartLineSnapshot(i.getMenuItemId(), i.getQuantity())).toList();
        if (lines.isEmpty() || cart.getRestaurantId() == null) throw new EmptyCartException();
        return new CartSnapshot(cart.getRestaurantId(), cart.getVersion(), lines);
    }
    private CustomerCart lockOrCreate(UUID customerId) {
        return cartRepository.findByCustomerIdForUpdate(customerId).orElseGet(() -> {
            CustomerCart newCart = cartRepository.saveAndFlush(new CustomerCart(customerId));
            entityManager.refresh(newCart);
            return newCart;
        });
    }
}
