package com.fooddelivery.order.repository;
import java.util.List;
import java.util.UUID;
import com.fooddelivery.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findAllByOrderIdOrderByItemNameAsc(UUID orderId);
    List<OrderItem> findAllByOrderIdInOrderByItemNameAsc(List<UUID> orderIds);
}
