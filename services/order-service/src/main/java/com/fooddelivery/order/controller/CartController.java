package com.fooddelivery.order.controller;
import java.util.UUID;
import com.fooddelivery.order.dto.ApiResponse;
import com.fooddelivery.order.dto.request.CartItemRequest;
import com.fooddelivery.order.dto.request.CartQuantityRequest;
import com.fooddelivery.order.dto.response.CartResponse;
import com.fooddelivery.order.service.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
@RestController @RequestMapping("/api/v1/orders/cart")
@PreAuthorize("hasRole('CUSTOMER')") @SecurityRequirement(name = "bearerAuth")
public class CartController {
    private final CartService cartService;
    public CartController(CartService cartService) { this.cartService = cartService; }
    @GetMapping public ApiResponse<CartResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Cart retrieved successfully", cartService.get(userId(jwt)));
    }
    @PostMapping("/items") @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CartResponse> add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CartItemRequest request) {
        return ApiResponse.success("Item added to cart", cartService.add(userId(jwt), request.menuItemId(), request.quantity()));
    }
    @PutMapping("/items/{menuItemId}")
    public ApiResponse<CartResponse> update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID menuItemId,
            @Valid @RequestBody CartQuantityRequest request) {
        return ApiResponse.success("Cart quantity updated", cartService.updateQuantity(userId(jwt), menuItemId, request.quantity()));
    }
    @DeleteMapping("/items/{menuItemId}")
    public ApiResponse<CartResponse> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID menuItemId) {
        return ApiResponse.success("Item removed from cart", cartService.remove(userId(jwt), menuItemId));
    }
    @DeleteMapping public ApiResponse<CartResponse> clear(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success("Cart cleared", cartService.clear(userId(jwt)));
    }
    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
