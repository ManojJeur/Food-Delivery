package com.fooddelivery.order.repository;
import java.util.Optional;
import java.util.UUID;
import com.fooddelivery.order.entity.FoodOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface FoodOrderRepository extends JpaRepository<FoodOrder, UUID> {
    Page<FoodOrder> findAllByCustomerId(UUID customerId, Pageable pageable);
    Optional<FoodOrder> findByIdAndCustomerId(UUID orderId, UUID customerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from FoodOrder o where o.id = :orderId")
    Optional<FoodOrder> findByIdForUpdate(@Param("orderId") UUID orderId);
}
