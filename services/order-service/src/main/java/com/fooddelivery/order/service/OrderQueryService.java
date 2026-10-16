package com.fooddelivery.order.service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.fooddelivery.order.entity.FoodOrder;
import com.fooddelivery.order.entity.OrderItem;
import com.fooddelivery.order.entity.OrderStatusHistory;
import com.fooddelivery.order.exception.OrderNotFoundException;
import com.fooddelivery.order.mapper.OrderMapper;
import com.fooddelivery.order.dto.response.OrderResponse;
import com.fooddelivery.order.repository.FoodOrderRepository;
import com.fooddelivery.order.repository.OrderItemRepository;
import com.fooddelivery.order.repository.OrderStatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class OrderQueryService {
    private final FoodOrderRepository orders;
    private final OrderItemRepository items;
    private final OrderStatusHistoryRepository history;
    private final OrderMapper mapper;
    public OrderQueryService(FoodOrderRepository orders, OrderItemRepository items, OrderStatusHistoryRepository history, OrderMapper mapper) {
        this.orders = orders; this.items = items; this.history = history; this.mapper = mapper;
    }
    @Transactional(readOnly = true)
    public OrderResponse getCustomerOrder(UUID customerId, UUID orderId) {
        FoodOrder order = orders.findByIdAndCustomerId(orderId, customerId).orElseThrow(() -> new OrderNotFoundException(orderId));
        return toResponse(order);
    }
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        return toResponse(orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId)));
    }
    @Transactional(readOnly = true)
    public FoodOrder findOrder(UUID orderId) { return orders.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId)); }
    @Transactional(readOnly = true)
    public Page<OrderResponse> mine(UUID customerId, Pageable pageable) {
        Page<FoodOrder> page = orders.findAllByCustomerId(customerId, pageable);
        List<UUID> ids = page.getContent().stream().map(FoodOrder::getId).toList();
        if (ids.isEmpty()) return page.map(order -> mapper.toResponse(order, List.of(), List.of()));
        Map<UUID, List<OrderItem>> itemMap = items.findAllByOrderIdInOrderByItemNameAsc(ids).stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        Map<UUID, List<OrderStatusHistory>> historyMap = history.findAllByOrderIdInOrderByChangedAtAsc(ids).stream().collect(Collectors.groupingBy(OrderStatusHistory::getOrderId));
        return page.map(order -> mapper.toResponse(order, itemMap.getOrDefault(order.getId(), List.of()), historyMap.getOrDefault(order.getId(), List.of())));
    }
    private OrderResponse toResponse(FoodOrder order) {
        return mapper.toResponse(order, items.findAllByOrderIdOrderByItemNameAsc(order.getId()), history.findAllByOrderIdOrderByChangedAtAsc(order.getId()));
    }
}
