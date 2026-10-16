package com.fooddelivery.menu.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import com.fooddelivery.menu.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    @Query("""
            select i from MenuItem i
            where i.restaurantId = :restaurantId
              and i.active = true
              and i.available = true
              and exists (select c.id from MenuCategory c where c.id = i.categoryId and c.active = true)
              and (:keyword = '' or lower(i.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or i.categoryId = :categoryId)
              and (:vegetarian is null or i.vegetarian = :vegetarian)
              and (:minPrice is null or i.price >= :minPrice)
              and (:maxPrice is null or i.price <= :maxPrice)
            """)
    Page<MenuItem> searchAvailable(@Param("restaurantId") UUID restaurantId,
            @Param("keyword") String keyword, @Param("categoryId") UUID categoryId,
            @Param("vegetarian") Boolean vegetarian, @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    @Query("""
            select i from MenuItem i
            where i.restaurantId = :restaurantId
              and (:keyword = '' or lower(i.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or i.categoryId = :categoryId)
              and (:vegetarian is null or i.vegetarian = :vegetarian)
            """)
    Page<MenuItem> searchManagement(@Param("restaurantId") UUID restaurantId,
            @Param("keyword") String keyword, @Param("categoryId") UUID categoryId,
            @Param("vegetarian") Boolean vegetarian, Pageable pageable);

    @Query("""
            select i from MenuItem i
            where i.id = :itemId and i.active = true and i.available = true
              and exists (select c.id from MenuCategory c where c.id = i.categoryId and c.active = true)
            """)
    Optional<MenuItem> findPublicById(@Param("itemId") UUID itemId);

    @Query("""
            select i from MenuItem i
            where i.id in :itemIds and i.active = true and i.available = true
              and exists (select c.id from MenuCategory c where c.id = i.categoryId and c.active = true)
            """)
    List<MenuItem> findAllAvailableByIdIn(@Param("itemIds") List<UUID> itemIds);
}
