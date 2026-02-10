package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Address;
import com.ecommerce_backend.Payloads.AddressDto;

import java.util.List;

public interface AddressService {
    AddressDto createAddress(AddressDto addressDto);

    List<AddressDto> getAllAddresses();

    AddressDto getAddress(Long addressId);

    List<AddressDto> getUserAddresses();

    AddressDto updateAddress(Long addressId, AddressDto addressDto);

    void deleteAddress(Long addressId);

    Address getAddressById(Long addressId);

    AddressDto buildAddressDto(Address address);
}
