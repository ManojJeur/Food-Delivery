package com.fooddelivery.order.entity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import jakarta.persistence.*;
@Entity @Table(name = "order_status_history")
public class OrderStatusHistory {
    @Id private UUID id;
    @Column(name = "order_id", nullable = false, updatable = false) private UUID orderId;
    @Enumerated(EnumType.STRING) @Column(name = "from_status", length = 32, updatable = false) private OrderStatus fromStatus;
    @Enumerated(EnumType.STRING) @Column(name = "to_status", nullable = false, length = 32, updatable = false) private OrderStatus toStatus;
    @Column(name = "changed_by", nullable = false, updatable = false) private UUID changedBy;
    @Column(name = "changed_at", nullable = false, updatable = false) private OffsetDateTime changedAt;
    protected OrderStatusHistory() { }
    public OrderStatusHistory(UUID orderId, OrderStatus fromStatus, OrderStatus toStatus, UUID changedBy) {
        this.id = UUID.randomUUID(); this.orderId = orderId; this.fromStatus = fromStatus; this.toStatus = toStatus;
        this.changedBy = changedBy; this.changedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public OrderStatus getFromStatus() { return fromStatus; }
    public OrderStatus getToStatus() { return toStatus; }
    public UUID getChangedBy() { return changedBy; }
    public OffsetDateTime getChangedAt() { return changedAt; }
}
