package com.fooddelivery.order.entity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import jakarta.persistence.*;
@Entity @Table(name = "customer_carts")
public class CustomerCart {
    @Id private UUID id;
    @Column(name = "customer_id", nullable = false, unique = true, updatable = false) private UUID customerId;
    @Column(name = "restaurant_id") private UUID restaurantId;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @Version @Column(nullable = false) private long version;
    protected CustomerCart() { }
    public CustomerCart(UUID customerId) { this.id = UUID.randomUUID(); this.customerId = customerId; }
    @PrePersist void onCreate() { OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
    public void touch() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
    public void setRestaurantId(UUID restaurantId) { this.restaurantId = restaurantId; }
    public void clearRestaurant() { this.restaurantId = null; }
    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getRestaurantId() { return restaurantId; }
    public long getVersion() { return version; }
}
