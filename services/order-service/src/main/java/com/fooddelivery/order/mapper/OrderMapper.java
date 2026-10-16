package com.fooddelivery.order.mapper;
import java.util.List;
import com.fooddelivery.order.dto.response.OrderItemResponse;
import com.fooddelivery.order.dto.response.OrderResponse;
import com.fooddelivery.order.dto.response.OrderStatusHistoryResponse;
import com.fooddelivery.order.entity.FoodOrder;
import com.fooddelivery.order.entity.OrderItem;
import com.fooddelivery.order.entity.OrderStatusHistory;
import org.springframework.stereotype.Component;
@Component
public class OrderMapper {
    public OrderResponse toResponse(FoodOrder order, List<OrderItem> items, List<OrderStatusHistory> history) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getRestaurantId(), order.getDeliveryAddress(),
                order.getSubtotal(), order.getDeliveryFee(), order.getTaxAmount(), order.getTotalAmount(), order.getStatus(),
                items.stream().map(this::toResponse).toList(), history.stream().map(this::toResponse).toList(),
                order.getCreatedAt(), order.getUpdatedAt());
    }
    private OrderItemResponse toResponse(OrderItem item) {
        return new OrderItemResponse(item.getMenuItemId(), item.getItemName(), item.getUnitPrice(), item.getQuantity(),
                item.getLineTotal(), item.isVegetarian());
    }
    private OrderStatusHistoryResponse toResponse(OrderStatusHistory entry) {
        return new OrderStatusHistoryResponse(entry.getFromStatus(), entry.getToStatus(), entry.getChangedBy(), entry.getChangedAt());
    }
}
