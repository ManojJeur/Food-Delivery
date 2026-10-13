package com.fooddelivery.restaurant.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurants_owner_id", columnList = "owner_id"),
        @Index(name = "idx_restaurants_status_name", columnList = "status,name")
})
public class Restaurant {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RestaurantStatus status;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Restaurant() {
    }

    public Restaurant(UUID ownerId, String name, String description, String phone, String email, String address,
            BigDecimal latitude, BigDecimal longitude, LocalTime openingTime, LocalTime closingTime) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.status = RestaurantStatus.CLOSED;
        updateDetails(name, description, phone, email, address, latitude, longitude, openingTime, closingTime);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = RestaurantStatus.CLOSED;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public RestaurantStatus getStatus() { return status; }
    public LocalTime getOpeningTime() { return openingTime; }
    public LocalTime getClosingTime() { return closingTime; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void updateDetails(String name, String description, String phone, String email, String address,
            BigDecimal latitude, BigDecimal longitude, LocalTime openingTime, LocalTime closingTime) {
        this.name = name;
        this.description = description;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public void changeStatus(RestaurantStatus status) {
        this.status = status;
    }
}
