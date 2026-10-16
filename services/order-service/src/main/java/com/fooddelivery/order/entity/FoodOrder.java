package com.fooddelivery.order.entity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import jakarta.persistence.*;
@Entity @Table(name = "orders")
public class FoodOrder {
    @Id private UUID id;
    @Column(name = "customer_id", nullable = false, updatable = false) private UUID customerId;
    @Column(name = "restaurant_id", nullable = false, updatable = false) private UUID restaurantId;
    @Column(name = "delivery_address", nullable = false, length = 1000) private String deliveryAddress;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    @Column(name = "delivery_fee", nullable = false, precision = 12, scale = 2) private BigDecimal deliveryFee;
    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2) private BigDecimal taxAmount;
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2) private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private OrderStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    @Version @Column(nullable = false) private long version;
    protected FoodOrder() { }
    public FoodOrder(UUID customerId, UUID restaurantId, String deliveryAddress, BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal taxAmount) {
        this.id = UUID.randomUUID(); this.customerId = customerId; this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress; this.subtotal = subtotal; this.deliveryFee = deliveryFee;
        this.taxAmount = taxAmount; this.totalAmount = subtotal.add(deliveryFee).add(taxAmount); this.status = OrderStatus.PENDING;
    }
    @PrePersist void onCreate() { OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = OffsetDateTime.now(ZoneOffset.UTC); }
    public void transitionTo(OrderStatus status) { this.status = status; }
    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getRestaurantId() { return restaurantId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
