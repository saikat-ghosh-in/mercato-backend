package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Address;
import com.ecommerce_backend.Payloads.Request.AddressRequestDTO;
import com.ecommerce_backend.Payloads.Response.AddressResponseDTO;

import java.util.List;

public interface AddressService {
    AddressResponseDTO createAddress(AddressRequestDTO addressRequestDTO);

    List<AddressResponseDTO> getAllAddresses();

    AddressResponseDTO getAddress(String addressId);

    List<AddressResponseDTO> getUserAddresses();

    AddressResponseDTO updateAddress(String addressId, AddressRequestDTO addressRequestDTO);

    void deleteAddress(String addressId);

    Address getAddressById(String addressId);

    AddressResponseDTO buildAddressResponseDTO(Address address);
}
