package com.fooddelivery.order.service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import com.fooddelivery.order.client.MenuClient;
import com.fooddelivery.order.client.MenuItemQuote;
import com.fooddelivery.order.client.RestaurantClient;
import com.fooddelivery.order.dto.CartSnapshot;
import com.fooddelivery.order.dto.response.OrderResponse;
import com.fooddelivery.order.exception.IdempotencyKeyConflictException;
import com.fooddelivery.order.exception.MenuItemsUnavailableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {
    private final CheckoutTransactionService transactions;
    private final CartMutationService carts;
    private final MenuClient menuClient;
    private final RestaurantClient restaurantClient;
    private final OrderQueryService orderQueries;
    public CheckoutService(CheckoutTransactionService transactions, CartMutationService carts,
            MenuClient menuClient, RestaurantClient restaurantClient, OrderQueryService orderQueries) {
        this.transactions = transactions; this.carts = carts; this.menuClient = menuClient;
        this.restaurantClient = restaurantClient; this.orderQueries = orderQueries;
    }
    public OrderResponse checkout(UUID customerId, String idempotencyKey, String deliveryAddress) {
        String key = idempotencyKey == null ? "" : idempotencyKey.trim();
        String address = deliveryAddress.trim();
        String hash = hash(address);
        CheckoutTransactionService.CheckoutLookup previous = transactions.findIdempotency(customerId, key);
        if (previous != null) return resolvePrevious(customerId, previous, hash);
        CartSnapshot cart = carts.snapshotForCheckout(customerId);
        restaurantClient.requireOpen(cart.restaurantId());
        List<UUID> itemIds = cart.items().stream().map(line -> line.menuItemId()).toList();
        List<MenuItemQuote> quotes = menuClient.quote(itemIds);
        if (quotes.size() != itemIds.size()) throw new MenuItemsUnavailableException();
        UUID orderId;
        try {
            orderId = transactions.checkout(customerId, key, hash, address, cart, quotes);
        } catch (DataIntegrityViolationException concurrentDuplicate) {
            CheckoutTransactionService.CheckoutLookup raced = transactions.findIdempotency(customerId, key);
            if (raced == null) throw concurrentDuplicate;
            return resolvePrevious(customerId, raced, hash);
        }
        return orderQueries.getCustomerOrder(customerId, orderId);
    }
    private OrderResponse resolvePrevious(UUID customerId, CheckoutTransactionService.CheckoutLookup previous, String hash) {
        if (!previous.requestHash().equals(hash)) throw new IdempotencyKeyConflictException();
        if (previous.orderId() == null) throw new IllegalStateException("Completed idempotency record has no order");
        return orderQueries.getCustomerOrder(customerId, previous.orderId());
    }
    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
