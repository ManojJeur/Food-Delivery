package com.fooddelivery.order.controller;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.fooddelivery.order.dto.ApiResponse;
import com.fooddelivery.order.dto.PageResponse;
import com.fooddelivery.order.dto.request.CheckoutRequest;
import com.fooddelivery.order.dto.request.OrderStatusRequest;
import com.fooddelivery.order.dto.response.OrderResponse;
import com.fooddelivery.order.entity.OrderStatus;
import com.fooddelivery.order.service.CheckoutService;
import com.fooddelivery.order.service.OrderQueryService;
import com.fooddelivery.order.service.OrderTransitionService;
import com.fooddelivery.order.util.Pagination;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
@Validated @RestController @RequestMapping("/api/v1/orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private final CheckoutService checkoutService;
    private final OrderQueryService orderQueries;
    private final OrderTransitionService transitions;
    public OrderController(CheckoutService checkoutService, OrderQueryService orderQueries, OrderTransitionService transitions) {
        this.checkoutService = checkoutService; this.orderQueries = orderQueries; this.transitions = transitions;
    }
    @PostMapping("/checkout") @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<OrderResponse> checkout(@AuthenticationPrincipal Jwt jwt,
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String idempotencyKey,
            @Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.success("Order placed successfully", checkoutService.checkout(userId(jwt), idempotencyKey, request.deliveryAddress()));
    }
    @GetMapping("/mine") @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<PageResponse<OrderResponse>> mine(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Pageable pageable = Pagination.of(page, size, sort);
        Page<OrderResponse> results = orderQueries.mine(userId(jwt), pageable);
        return ApiResponse.success("Orders retrieved successfully", PageResponse.from(results));
    }
    @GetMapping("/{orderId}") @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<OrderResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        OrderResponse response = hasRole(jwt, "ADMIN") ? orderQueries.getOrder(orderId)
                : orderQueries.getCustomerOrder(userId(jwt), orderId);
        return ApiResponse.success("Order retrieved successfully", response);
    }
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_PARTNER', 'ADMIN')")
    public ApiResponse<OrderResponse> changeStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusRequest request) {
        return ApiResponse.success("Order status updated", transitions.transition(orderId, userId(jwt),
                request.status(), roles(jwt), jwt.getTokenValue()));
    }
    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
    private Set<String> roles(Jwt jwt) {
        List<String> claim = jwt.getClaimAsStringList("roles"); return claim == null ? Set.of() : Set.copyOf(claim);
    }
    private boolean hasRole(Jwt jwt, String role) { return roles(jwt).contains(role); }
}
