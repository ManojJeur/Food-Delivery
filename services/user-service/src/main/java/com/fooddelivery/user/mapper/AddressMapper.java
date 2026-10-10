package com.fooddelivery.user.mapper;

import com.fooddelivery.user.dto.request.AddressRequest;
import com.fooddelivery.user.dto.response.AddressResponse;
import com.fooddelivery.user.entity.AppUser;
import com.fooddelivery.user.entity.UserAddress;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public UserAddress toEntity(AddressRequest request, AppUser user) {
        return new UserAddress(user, request.label(), request.recipientName().trim(), request.phone().trim(),
                request.line1().trim(), trimToNull(request.line2()), request.city().trim(), request.state().trim(),
                request.postalCode().trim(), request.countryCode());
    }

    public void updateEntity(UserAddress address, AddressRequest request) {
        address.update(request.label(), request.recipientName().trim(), request.phone().trim(),
                request.line1().trim(), trimToNull(request.line2()), request.city().trim(), request.state().trim(),
                request.postalCode().trim(), request.countryCode());
    }

    public AddressResponse toResponse(UserAddress address) {
        return new AddressResponse(address.getId(), address.getLabel(), address.getRecipientName(), address.getPhone(),
                address.getLine1(), address.getLine2(), address.getCity(), address.getState(), address.getPostalCode(),
                address.getCountryCode(), address.getCreatedAt(), address.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
