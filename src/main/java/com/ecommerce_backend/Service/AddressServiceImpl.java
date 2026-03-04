package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Address;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.ExceptionHandler.ForbiddenOperationException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.Request.AddressRequestDTO;
import com.ecommerce_backend.Payloads.Response.AddressResponseDTO;
import com.ecommerce_backend.Repository.AddressRepository;
import com.ecommerce_backend.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDTO createAddress(AddressRequestDTO addressRequestDTO) {
        EcommUser currentUser = authUtil.getLoggedInUser();

        Address newAddress = new Address();
        newAddress.setRecipientName(addressRequestDTO.getRecipientName());
        newAddress.setRecipientPhone(addressRequestDTO.getRecipientPhone());
        newAddress.setAddressLine1(addressRequestDTO.getAddressLine1());
        newAddress.setAddressLine2(addressRequestDTO.getAddressLine2());
        newAddress.setCity(addressRequestDTO.getCity());
        newAddress.setState(addressRequestDTO.getState());
        newAddress.setPincode(addressRequestDTO.getPincode());
        newAddress.setCountry(addressRequestDTO.getCountry());

        currentUser.addAddress(newAddress);

        Address savedAddress = addressRepository.save(newAddress);
        return buildAddressResponseDTO(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(this::buildAddressResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDTO getAddress(String addressId) {
        Address address = getAddressById(addressId); // throws
        return this.buildAddressResponseDTO(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getUserAddresses() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        List<Address> addresses = currentUser.getAddresses();
        return addresses.stream()
                .map(this::buildAddressResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponseDTO updateAddress(String addressId, AddressRequestDTO addressRequestDTO) {
        Address address = getAddressById(addressId);
        EcommUser currentUser = authUtil.getLoggedInUser();

        if (address.getUser() == null) {
            throw new IllegalStateException("Address is not associated with any user");
        }
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot update this address");
        }

        address.setRecipientName(addressRequestDTO.getRecipientName());
        address.setRecipientPhone(addressRequestDTO.getRecipientPhone());
        address.setAddressLine1(addressRequestDTO.getAddressLine1());
        address.setAddressLine2(addressRequestDTO.getAddressLine2());
        address.setCity(addressRequestDTO.getCity());
        address.setState(addressRequestDTO.getState());
        address.setPincode(addressRequestDTO.getPincode());
        address.setCountry(addressRequestDTO.getCountry());

        return buildAddressResponseDTO(address);
    }

    @Override
    @Transactional
    public void deleteAddress(String addressId) {
        Address address = getAddressById(addressId);
        EcommUser currentUser = authUtil.getLoggedInUser();

        if (address.getUser() == null) {
            throw new IllegalStateException("Address is not associated with any user");
        }
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You cannot delete this address");
        }

        currentUser.removeAddress(address);
    }

    @Override
    public Address getAddressById(String addressId) {
        return addressRepository.findByAddressId(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
    }

    @Override
    public AddressResponseDTO buildAddressResponseDTO(Address address) {
        return new AddressResponseDTO(
                address.getAddressId(),
                address.getUser().getUserId(),
                address.getUser().getUsername(),
                address.getRecipientName(),
                address.getRecipientPhone(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getCountry()
        );
    }
}
