package com.fooddelivery.menu.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    private UUID id;

    @Column(name = "restaurant_id", nullable = false, updatable = false)
    private UUID restaurantId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(nullable = false)
    private boolean vegetarian;

    @Column(nullable = false)
    private boolean available;

    @Column(name = "preparation_time_minutes", nullable = false)
    private short preparationTimeMinutes;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected MenuItem() {
    }

    public MenuItem(UUID restaurantId, UUID categoryId, String name, String description, BigDecimal price,
            String imageUrl, boolean vegetarian, int preparationTimeMinutes) {
        this.id = UUID.randomUUID();
        this.restaurantId = restaurantId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.vegetarian = vegetarian;
        this.available = false;
        this.preparationTimeMinutes = (short) preparationTimeMinutes;
        this.active = true;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void update(UUID categoryId, String name, String description, BigDecimal price, String imageUrl,
            boolean vegetarian, int preparationTimeMinutes) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.vegetarian = vegetarian;
        this.preparationTimeMinutes = (short) preparationTimeMinutes;
    }

    public void changeAvailability(boolean available) {
        this.available = available;
    }

    public void deactivate() {
        this.active = false;
        this.available = false;
    }

    public UUID getId() { return id; }
    public UUID getRestaurantId() { return restaurantId; }
    public UUID getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public boolean isVegetarian() { return vegetarian; }
    public boolean isAvailable() { return available; }
    public int getPreparationTimeMinutes() { return preparationTimeMinutes; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
