package com.fooddelivery.order.service;
import java.util.Set;
import java.util.UUID;
import com.fooddelivery.order.client.RestaurantClient;
import com.fooddelivery.order.dto.response.OrderResponse;
import com.fooddelivery.order.entity.FoodOrder;
import com.fooddelivery.order.entity.OrderStatus;
import org.springframework.stereotype.Service;
@Service
public class OrderTransitionService {
    private final OrderQueryService queries;
    private final OrderTransitionTransactionService transactions;
    private final RestaurantClient restaurants;
    public OrderTransitionService(OrderQueryService queries, OrderTransitionTransactionService transactions,
            RestaurantClient restaurants) {
        this.queries = queries; this.transactions = transactions; this.restaurants = restaurants;
    }
    public OrderResponse transition(UUID orderId, UUID actorId, OrderStatus target, Set<String> roles, String token) {
        if (!roles.contains("ADMIN") && roles.contains("RESTAURANT_OWNER") && restaurantStage(target)) {
            FoodOrder order = queries.findOrder(orderId);
            restaurants.assertCanManage(order.getRestaurantId(), token);
        }
        UUID changed = transactions.transition(orderId, actorId, target, roles);
        return queries.getOrder(changed);
    }
    private boolean restaurantStage(OrderStatus target) {
        return target == OrderStatus.CONFIRMED || target == OrderStatus.RESTAURANT_ACCEPTED
                || target == OrderStatus.PREPARING || target == OrderStatus.READY_FOR_PICKUP;
    }
}
