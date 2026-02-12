package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Address;
import com.ecommerce_backend.Payloads.Request.AddressRequestDTO;
import com.ecommerce_backend.Payloads.Response.AddressResponseDTO;

import java.util.List;

public interface AddressService {
    AddressResponseDTO createAddress(AddressRequestDTO addressRequestDTO);

    List<AddressResponseDTO> getAllAddresses();

    AddressResponseDTO getAddress(Long addressId);

    List<AddressResponseDTO> getUserAddresses();

    AddressResponseDTO updateAddress(Long addressId, AddressRequestDTO addressRequestDTO);

    void deleteAddress(Long addressId);

    Address getAddressById(Long addressId);

    AddressResponseDTO buildAddressResponseDTO(Address address);
}
