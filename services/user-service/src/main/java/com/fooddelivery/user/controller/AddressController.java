package com.fooddelivery.user.controller;

import java.util.List;
import java.util.UUID;

import com.fooddelivery.user.dto.ApiResponse;
import com.fooddelivery.user.dto.request.AddressRequest;
import com.fooddelivery.user.dto.response.AddressResponse;
import com.fooddelivery.user.mapper.AddressMapper;
import com.fooddelivery.user.service.AddressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;
    private final AddressMapper addressMapper;

    public AddressController(AddressService addressService, AddressMapper addressMapper) {
        this.addressService = addressService;
        this.addressMapper = addressMapper;
    }

    @GetMapping
    public ApiResponse<List<AddressResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<AddressResponse> addresses = addressService.list(userId).stream().map(addressMapper::toResponse).toList();
        return ApiResponse.success("Addresses retrieved successfully", addresses);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> create(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddressRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success("Address created successfully",
                addressMapper.toResponse(addressService.create(userId, request)));
    }

    @PutMapping("/{addressId}")
    public ApiResponse<AddressResponse> update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ApiResponse.success("Address updated successfully",
                addressMapper.toResponse(addressService.update(userId, addressId, request)));
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId) {
        addressService.delete(UUID.fromString(jwt.getSubject()), addressId);
    }
}
