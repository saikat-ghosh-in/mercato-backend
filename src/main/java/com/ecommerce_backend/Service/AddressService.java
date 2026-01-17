package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.AddressDto;

import java.util.List;

public interface AddressService {
    AddressDto createAddress(AddressDto addressDto);

    List<AddressDto> getAllAddresses();

    AddressDto getAddress(Long addressId);

    List<AddressDto> getUserAddresses();

    AddressDto updateAddress(AddressDto addressDto);

    void deleteAddress(Long addressId);
}
