package com.fooddelivery.order.repository;
import java.util.Optional;
import java.util.UUID;
import com.fooddelivery.order.entity.CheckoutIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CheckoutIdempotencyRepository extends JpaRepository<CheckoutIdempotency, UUID> {
    Optional<CheckoutIdempotency> findByCustomerIdAndIdempotencyKey(UUID customerId, String idempotencyKey);
}
