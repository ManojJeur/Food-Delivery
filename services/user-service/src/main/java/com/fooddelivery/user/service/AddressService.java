package com.fooddelivery.user.service;

import java.util.List;
import java.util.UUID;

import com.fooddelivery.user.dto.request.AddressRequest;
import com.fooddelivery.user.entity.AppUser;
import com.fooddelivery.user.entity.UserAddress;
import com.fooddelivery.user.exception.AddressNotFoundException;
import com.fooddelivery.user.exception.UserNotFoundException;
import com.fooddelivery.user.mapper.AddressMapper;
import com.fooddelivery.user.repository.UserAddressRepository;
import com.fooddelivery.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final AddressMapper addressMapper;

    public AddressService(UserRepository userRepository, UserAddressRepository addressRepository,
            AddressMapper addressMapper) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
    }

    @Transactional(readOnly = true)
    public List<UserAddress> list(UUID userId) {
        return addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public UserAddress create(UUID userId, AddressRequest request) {
        AppUser user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return addressRepository.save(addressMapper.toEntity(request, user));
    }

    @Transactional
    public UserAddress update(UUID userId, UUID addressId, AddressRequest request) {
        UserAddress address = findOwnedAddress(userId, addressId);
        addressMapper.updateEntity(address, request);
        return address;
    }

    @Transactional
    public void delete(UUID userId, UUID addressId) {
        UserAddress address = findOwnedAddress(userId, addressId);
        addressRepository.delete(address);
    }

    private UserAddress findOwnedAddress(UUID userId, UUID addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
    }
}
