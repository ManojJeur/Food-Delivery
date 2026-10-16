package com.fooddelivery.order.repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.fooddelivery.order.entity.CartItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findAllByCartIdOrderByItemNameAsc(UUID cartId);
    Optional<CartItem> findByCartIdAndMenuItemId(UUID cartId, UUID menuItemId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from CartItem i where i.cartId = :cartId and i.menuItemId = :itemId")
    Optional<CartItem> findForUpdate(@Param("cartId") UUID cartId, @Param("itemId") UUID itemId);
    void deleteAllByCartId(UUID cartId);
    long countByCartId(UUID cartId);
}
