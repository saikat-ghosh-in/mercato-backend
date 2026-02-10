package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Address;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.AddressDto;
import com.ecommerce_backend.Repository.AddressRepository;
import com.ecommerce_backend.Utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public AddressDto createAddress(AddressDto addressDto) {
        EcommUser currentUser = authUtil.getLoggedInUser();

        Address newAddress = new Address();
        newAddress.setRecipientName(addressDto.getRecipientName());
        newAddress.setRecipientPhone(addressDto.getRecipientPhone());
        newAddress.setAddressLine1(addressDto.getAddressLine1());
        newAddress.setAddressLine2(addressDto.getAddressLine2());
        newAddress.setCity(addressDto.getCity());
        newAddress.setState(addressDto.getState());
        newAddress.setPincode(addressDto.getPincode());
        newAddress.setCountry(addressDto.getCountry());

        currentUser.addAddress(newAddress);

        Address savedAddress = addressRepository.save(newAddress);
        return buildAddressDto(savedAddress);
    }

    @Override
    public List<AddressDto> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(this::buildAddressDto)
                .toList();
    }

    @Override
    public AddressDto getAddress(Long addressId) {
        Address address = getAddressById(addressId); // throws
        return buildAddressDto(address);
    }

    @Override
    public List<AddressDto> getUserAddresses() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        List<Address> addresses = currentUser.getAddresses();
        return addresses.stream()
                .map(this::buildAddressDto)
                .toList();
    }

    @Override
    @Transactional
    public AddressDto updateAddress(Long addressId, AddressDto addressDto) {
        Address address = getAddressById(addressId);
        EcommUser currentUser = authUtil.getLoggedInUser();

        if (address.getUser() == null) {
            throw new IllegalStateException("Address is not associated with any user");
        }
        if (!address.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You cannot update this address");
        }

        address.setRecipientName(addressDto.getRecipientName());
        address.setRecipientPhone(addressDto.getRecipientPhone());
        address.setAddressLine1(addressDto.getAddressLine1());
        address.setAddressLine2(addressDto.getAddressLine2());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setPincode(addressDto.getPincode());
        address.setCountry(addressDto.getCountry());

        return buildAddressDto(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        Address address = getAddressById(addressId);
        EcommUser currentUser = authUtil.getLoggedInUser();

        if (address.getUser() == null) {
            throw new IllegalStateException("Address is not associated with any user");
        }
        if (!address.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You cannot delete this address");
        }

        currentUser.removeAddress(address);
    }

    @Override
    public Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
    }

    @Override
    public AddressDto buildAddressDto(Address address) {
        return new AddressDto(
                address.getAddressId(),
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
