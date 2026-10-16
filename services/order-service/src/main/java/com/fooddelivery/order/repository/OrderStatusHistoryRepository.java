package com.fooddelivery.order.repository;
import java.util.List;
import java.util.UUID;
import com.fooddelivery.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {
    List<OrderStatusHistory> findAllByOrderIdOrderByChangedAtAsc(UUID orderId);
    List<OrderStatusHistory> findAllByOrderIdInOrderByChangedAtAsc(List<UUID> orderIds);
}
