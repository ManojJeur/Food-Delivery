package com.fooddelivery.order.entity;
import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.*;
@Entity @Table(name = "order_items")
public class OrderItem {
    @Id private UUID id;
    @Column(name = "order_id", nullable = false, updatable = false) private UUID orderId;
    @Column(name = "menu_item_id", nullable = false, updatable = false) private UUID menuItemId;
    @Column(name = "item_name", nullable = false, length = 160, updatable = false) private String itemName;
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2, updatable = false) private BigDecimal unitPrice;
    @Column(nullable = false, updatable = false) private int quantity;
    @Column(name = "line_total", nullable = false, precision = 12, scale = 2, updatable = false) private BigDecimal lineTotal;
    @Column(nullable = false, updatable = false) private boolean vegetarian;
    protected OrderItem() { }
    public OrderItem(UUID orderId, UUID menuItemId, String itemName, BigDecimal unitPrice, int quantity, boolean vegetarian) {
        this.id = UUID.randomUUID(); this.orderId = orderId; this.menuItemId = menuItemId; this.itemName = itemName;
        this.unitPrice = unitPrice; this.quantity = quantity; this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity)); this.vegetarian = vegetarian;
    }
    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public UUID getMenuItemId() { return menuItemId; }
    public String getItemName() { return itemName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public boolean isVegetarian() { return vegetarian; }
}
