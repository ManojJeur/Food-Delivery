package com.fooddelivery.order.service;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.fooddelivery.order.entity.FoodOrder;
import com.fooddelivery.order.entity.OrderStatus;
import com.fooddelivery.order.entity.OrderStatusHistory;
import com.fooddelivery.order.exception.InvalidOrderTransitionException;
import com.fooddelivery.order.exception.OrderNotFoundException;
import com.fooddelivery.order.exception.UnauthorizedOperationException;
import com.fooddelivery.order.repository.FoodOrderRepository;
import com.fooddelivery.order.repository.OrderStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderTransitionTransactionService {
    private static final Map<OrderStatus, Set<OrderStatus>> NEXT = transitions();
    private final FoodOrderRepository orders;
    private final OrderStatusHistoryRepository history;
    public OrderTransitionTransactionService(FoodOrderRepository orders, OrderStatusHistoryRepository history) {
        this.orders = orders; this.history = history;
    }
    @Transactional
    public UUID transition(UUID orderId, UUID actorId, OrderStatus target, Set<String> roles) {
        FoodOrder order = orders.findByIdForUpdate(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        OrderStatus from = order.getStatus();
        if (!NEXT.getOrDefault(from, Set.of()).contains(target)) throw new InvalidOrderTransitionException();
        boolean admin = roles.contains("ADMIN");
        boolean customerCancellation = target == OrderStatus.CANCELLED && roles.contains("CUSTOMER")
                && order.getCustomerId().equals(actorId);
        boolean restaurantAction = isRestaurantAction(from, target) && roles.contains("RESTAURANT_OWNER");
        if (!admin && !customerCancellation && !restaurantAction) throw new UnauthorizedOperationException();
        order.transitionTo(target);
        history.save(new OrderStatusHistory(order.getId(), from, target, actorId));
        return order.getId();
    }
    private boolean isRestaurantAction(OrderStatus from, OrderStatus to) {
        return (from == OrderStatus.PENDING && to == OrderStatus.CONFIRMED)
                || (from == OrderStatus.CONFIRMED && to == OrderStatus.RESTAURANT_ACCEPTED)
                || (from == OrderStatus.RESTAURANT_ACCEPTED && to == OrderStatus.PREPARING)
                || (from == OrderStatus.PREPARING && to == OrderStatus.READY_FOR_PICKUP);
    }
    private static Map<OrderStatus, Set<OrderStatus>> transitions() {
        Map<OrderStatus, Set<OrderStatus>> map = new EnumMap<>(OrderStatus.class);
        map.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        map.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.RESTAURANT_ACCEPTED, OrderStatus.CANCELLED));
        map.put(OrderStatus.RESTAURANT_ACCEPTED, EnumSet.of(OrderStatus.PREPARING));
        map.put(OrderStatus.PREPARING, EnumSet.of(OrderStatus.READY_FOR_PICKUP));
        map.put(OrderStatus.READY_FOR_PICKUP, EnumSet.of(OrderStatus.PICKED_UP));
        map.put(OrderStatus.PICKED_UP, EnumSet.of(OrderStatus.OUT_FOR_DELIVERY));
        map.put(OrderStatus.OUT_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED));
        return Map.copyOf(map);
    }
}
