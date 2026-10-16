package com.fooddelivery.order.repository;
import java.util.Optional;
import java.util.UUID;
import com.fooddelivery.order.entity.CustomerCart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface CustomerCartRepository extends JpaRepository<CustomerCart, UUID> {
    Optional<CustomerCart> findByCustomerId(UUID customerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CustomerCart c where c.customerId = :customerId")
    Optional<CustomerCart> findByCustomerIdForUpdate(@Param("customerId") UUID customerId);
}
