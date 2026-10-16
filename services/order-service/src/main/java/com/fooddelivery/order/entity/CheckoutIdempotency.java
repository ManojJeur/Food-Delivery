package com.fooddelivery.order.entity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import jakarta.persistence.*;
@Entity @Table(name = "checkout_idempotency", uniqueConstraints = @UniqueConstraint(name = "uq_checkout_idempotency", columnNames = {"customer_id", "idempotency_key"}))
public class CheckoutIdempotency {
    @Id private UUID id;
    @Column(name = "customer_id", nullable = false, updatable = false) private UUID customerId;
    @Column(name = "idempotency_key", nullable = false, length = 100, updatable = false) private String idempotencyKey;
    @Column(name = "request_hash", nullable = false, length = 64, updatable = false) private String requestHash;
    @Column(name = "order_id") private UUID orderId;
    @Column(name = "created_at", nullable = false, updatable = false) private OffsetDateTime createdAt;
    protected CheckoutIdempotency() { }
    public CheckoutIdempotency(UUID customerId, String idempotencyKey, String requestHash) {
        this.id = UUID.randomUUID(); this.customerId = customerId; this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash; this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestHash() { return requestHash; }
    public UUID getOrderId() { return orderId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
