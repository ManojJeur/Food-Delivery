package com.fooddelivery.order.entity;
import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.*;
@Entity @Table(name = "cart_items")
public class CartItem {
    @Id private UUID id;
    @Column(name = "cart_id", nullable = false, updatable = false) private UUID cartId;
    @Column(name = "menu_item_id", nullable = false, updatable = false) private UUID menuItemId;
    @Column(name = "item_name", nullable = false, length = 160) private String itemName;
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2) private BigDecimal unitPrice;
    @Column(nullable = false) private int quantity;
    protected CartItem() { }
    public CartItem(UUID cartId, UUID menuItemId, String itemName, BigDecimal unitPrice, int quantity) {
        this.id = UUID.randomUUID(); this.cartId = cartId; this.menuItemId = menuItemId;
        this.itemName = itemName; this.unitPrice = unitPrice; this.quantity = quantity;
    }
    public void update(String itemName, BigDecimal unitPrice, int quantity) { this.itemName = itemName; this.unitPrice = unitPrice; this.quantity = quantity; }
    public UUID getId() { return id; }
    public UUID getCartId() { return cartId; }
    public UUID getMenuItemId() { return menuItemId; }
    public String getItemName() { return itemName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
