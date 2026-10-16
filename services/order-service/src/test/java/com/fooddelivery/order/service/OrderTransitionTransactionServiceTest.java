package com.fooddelivery.order.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.fooddelivery.order.entity.FoodOrder;
import com.fooddelivery.order.entity.OrderStatus;
import com.fooddelivery.order.exception.InvalidOrderTransitionException;
import com.fooddelivery.order.exception.UnauthorizedOperationException;
import com.fooddelivery.order.repository.FoodOrderRepository;
import com.fooddelivery.order.repository.OrderStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class OrderTransitionTransactionServiceTest {
    @Mock private FoodOrderRepository orders;
    @Mock private OrderStatusHistoryRepository history;
    private OrderTransitionTransactionService service;
    @BeforeEach void setUp() { service = new OrderTransitionTransactionService(orders, history); }
    @Test void restaurantOwnerCanConfirmRestaurantOrder() {
        UUID customer = UUID.randomUUID();
        FoodOrder order = order(customer);
        when(orders.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        UUID id = service.transition(order.getId(), UUID.randomUUID(), OrderStatus.CONFIRMED, Set.of("RESTAURANT_OWNER"));
        assertEquals(order.getId(), id);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(history).save(any());
    }
    @Test void customerCannotMoveOrderIntoRestaurantWorkflow() {
        FoodOrder order = order(UUID.randomUUID());
        when(orders.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        assertThrows(UnauthorizedOperationException.class,
                () -> service.transition(order.getId(), order.getCustomerId(), OrderStatus.CONFIRMED, Set.of("CUSTOMER")));
    }
    @Test void rejectsSkippingOrderStates() {
        FoodOrder order = order(UUID.randomUUID());
        when(orders.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        assertThrows(InvalidOrderTransitionException.class,
                () -> service.transition(order.getId(), UUID.randomUUID(), OrderStatus.DELIVERED, Set.of("ADMIN")));
    }
    private FoodOrder order(UUID customerId) {
        return new FoodOrder(customerId, UUID.randomUUID(), "address", new BigDecimal("12.00"),
                BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
