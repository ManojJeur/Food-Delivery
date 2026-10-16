package com.fooddelivery.order.service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fooddelivery.order.client.MenuItemQuote;
import com.fooddelivery.order.dto.CartLineSnapshot;
import com.fooddelivery.order.dto.CartSnapshot;
import com.fooddelivery.order.entity.CartItem;
import com.fooddelivery.order.entity.CheckoutIdempotency;
import com.fooddelivery.order.entity.CustomerCart;
import com.fooddelivery.order.entity.FoodOrder;
import com.fooddelivery.order.entity.OrderItem;
import com.fooddelivery.order.entity.OrderStatus;
import com.fooddelivery.order.entity.OrderStatusHistory;
import com.fooddelivery.order.exception.CartChangedException;
import com.fooddelivery.order.exception.EmptyCartException;
import com.fooddelivery.order.exception.IdempotencyKeyConflictException;
import com.fooddelivery.order.exception.MenuItemsUnavailableException;
import com.fooddelivery.order.repository.CartItemRepository;
import com.fooddelivery.order.repository.CheckoutIdempotencyRepository;
import com.fooddelivery.order.repository.CustomerCartRepository;
import com.fooddelivery.order.repository.FoodOrderRepository;
import com.fooddelivery.order.repository.OrderItemRepository;
import com.fooddelivery.order.repository.OrderStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class CheckoutTransactionService {
    private final CheckoutIdempotencyRepository idempotency;
    private final CustomerCartRepository carts;
    private final CartItemRepository cartItems;
    private final FoodOrderRepository orders;
    private final OrderItemRepository orderItems;
    private final OrderStatusHistoryRepository history;
    public CheckoutTransactionService(CheckoutIdempotencyRepository idempotency, CustomerCartRepository carts,
            CartItemRepository cartItems, FoodOrderRepository orders, OrderItemRepository orderItems,
            OrderStatusHistoryRepository history) {
        this.idempotency = idempotency; this.carts = carts; this.cartItems = cartItems;
        this.orders = orders; this.orderItems = orderItems; this.history = history;
    }
    @Transactional(readOnly = true)
    public CheckoutLookup findIdempotency(UUID customerId, String key) {
        return idempotency.findByCustomerIdAndIdempotencyKey(customerId, key)
                .map(row -> new CheckoutLookup(row.getRequestHash(), row.getOrderId())).orElse(null);
    }
    @Transactional
    public UUID checkout(UUID customerId, String key, String requestHash, String address,
            CartSnapshot snapshot, List<MenuItemQuote> quotes) {
        CheckoutIdempotency previous = idempotency.findByCustomerIdAndIdempotencyKey(customerId, key).orElse(null);
        if (previous != null) return existingOrder(previous, requestHash);
        CheckoutIdempotency reservation = idempotency.saveAndFlush(new CheckoutIdempotency(customerId, key, requestHash));
        CustomerCart cart = carts.findByCustomerIdForUpdate(customerId).orElseThrow(EmptyCartException::new);
        List<CartItem> lines = cartItems.findAllByCartIdOrderByItemNameAsc(cart.getId());
        if (lines.isEmpty() || cart.getRestaurantId() == null) throw new EmptyCartException();
        if (cart.getVersion() != snapshot.version() || !cart.getRestaurantId().equals(snapshot.restaurantId())
                || !sameLines(lines, snapshot.items())) throw new CartChangedException();
        Map<UUID, MenuItemQuote> quoteById = new HashMap<>();
        for (MenuItemQuote quote : quotes) {
            if (!quote.restaurantId().equals(cart.getRestaurantId())) throw new MenuItemsUnavailableException();
            quoteById.put(quote.id(), quote);
        }
        if (quoteById.size() != lines.size()) throw new MenuItemsUnavailableException();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem line : lines) {
            MenuItemQuote quote = quoteById.get(line.getMenuItemId());
            if (quote == null) throw new MenuItemsUnavailableException();
            subtotal = subtotal.add(quote.price().multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        BigDecimal zero = BigDecimal.ZERO.setScale(2);
        FoodOrder order = orders.saveAndFlush(new FoodOrder(customerId, cart.getRestaurantId(), address, subtotal, zero, zero));
        List<OrderItem> snapshots = lines.stream().map(line -> {
            MenuItemQuote quote = quoteById.get(line.getMenuItemId());
            return new OrderItem(order.getId(), quote.id(), quote.name(), quote.price(), line.getQuantity(), quote.vegetarian());
        }).toList();
        orderItems.saveAll(snapshots);
        history.save(new OrderStatusHistory(order.getId(), null, OrderStatus.PENDING, customerId));
        reservation.setOrderId(order.getId());
        idempotency.save(reservation);
        cartItems.deleteAllByCartId(cart.getId());
        cart.clearRestaurant(); cart.touch();
        return order.getId();
    }
    private UUID existingOrder(CheckoutIdempotency row, String hash) {
        if (!row.getRequestHash().equals(hash)) throw new IdempotencyKeyConflictException();
        if (row.getOrderId() == null) throw new IllegalStateException("Completed idempotency record has no order");
        return row.getOrderId();
    }
    private boolean sameLines(List<CartItem> items, List<CartLineSnapshot> snapshot) {
        if (items.size() != snapshot.size()) return false;
        Map<UUID, Integer> quantities = new HashMap<>();
        for (CartLineSnapshot line : snapshot) quantities.put(line.menuItemId(), line.quantity());
        return items.stream().allMatch(item -> quantities.getOrDefault(item.getMenuItemId(), -1) == item.getQuantity());
    }
    public record CheckoutLookup(String requestHash, UUID orderId) { }
}
