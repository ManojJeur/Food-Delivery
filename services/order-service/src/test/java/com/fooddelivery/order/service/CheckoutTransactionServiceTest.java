package com.fooddelivery.order.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.fooddelivery.order.client.MenuItemQuote;
import com.fooddelivery.order.dto.CartLineSnapshot;
import com.fooddelivery.order.dto.CartSnapshot;
import com.fooddelivery.order.entity.CartItem;
import com.fooddelivery.order.entity.CustomerCart;
import com.fooddelivery.order.entity.FoodOrder;
import com.fooddelivery.order.entity.OrderStatus;
import com.fooddelivery.order.repository.CartItemRepository;
import com.fooddelivery.order.repository.CheckoutIdempotencyRepository;
import com.fooddelivery.order.repository.CustomerCartRepository;
import com.fooddelivery.order.repository.FoodOrderRepository;
import com.fooddelivery.order.repository.OrderItemRepository;
import com.fooddelivery.order.repository.OrderStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class CheckoutTransactionServiceTest {
    @Mock private CheckoutIdempotencyRepository idempotency;
    @Mock private CustomerCartRepository carts;
    @Mock private CartItemRepository cartItems;
    @Mock private FoodOrderRepository orders;
    @Mock private OrderItemRepository orderItems;
    @Mock private OrderStatusHistoryRepository history;
    private CheckoutTransactionService service;
    @BeforeEach void setUp() { service = new CheckoutTransactionService(idempotency, carts, cartItems, orders, orderItems, history); }
    @Test void snapshotsFreshMenuPriceAndClearsCartAtomically() {
        UUID customerId = UUID.randomUUID(); UUID restaurantId = UUID.randomUUID(); UUID menuId = UUID.randomUUID();
        CustomerCart cart = new CustomerCart(customerId); cart.setRestaurantId(restaurantId);
        CartItem line = new CartItem(cart.getId(), menuId, "Old Name", new BigDecimal("2.00"), 3);
        when(idempotency.findByCustomerIdAndIdempotencyKey(customerId, "key-1")).thenReturn(Optional.empty());
        when(idempotency.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(carts.findByCustomerIdForUpdate(customerId)).thenReturn(Optional.of(cart));
        when(cartItems.findAllByCartIdOrderByItemNameAsc(cart.getId())).thenReturn(List.of(line));
        when(orders.saveAndFlush(any(FoodOrder.class))).thenAnswer(i -> i.getArgument(0));
        CartSnapshot snapshot = new CartSnapshot(restaurantId, 0, List.of(new CartLineSnapshot(menuId, 3)));
        MenuItemQuote quote = new MenuItemQuote(menuId, restaurantId, "Current Name", new BigDecimal("4.25"), true);

        UUID orderId = service.checkout(customerId, "key-1", "hash", "delivery address", snapshot, List.of(quote));

        ArgumentCaptor<FoodOrder> orderCaptor = ArgumentCaptor.forClass(FoodOrder.class);
        verify(orders).saveAndFlush(orderCaptor.capture());
        assertEquals(new BigDecimal("12.75"), orderCaptor.getValue().getSubtotal());
        assertEquals(orderCaptor.getValue().getId(), orderId);
        verify(orderItems).saveAll(any());
        verify(cartItems).deleteAllByCartId(cart.getId());
        verify(idempotency).save(any());
        assertEquals(OrderStatus.PENDING, orderCaptor.getValue().getStatus());
    }
}
