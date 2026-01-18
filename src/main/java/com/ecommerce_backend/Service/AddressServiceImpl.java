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
public class AddressServiceImpl implements AddressService{

    private final AddressRepository addressRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public AddressDto createAddress(AddressDto addressDto) {
        EcommUser currentUser = authUtil.getLoggedInUser();

        Address newAddress = new Address();
        newAddress.setStreet(addressDto.getStreet());
        newAddress.setCity(addressDto.getCity());
        newAddress.setState(addressDto.getState());
        newAddress.setPincode(addressDto.getPincode());
        newAddress.setCountry(addressDto.getCountry());

        currentUser.addAddress(newAddress);

        Address savedAddress = addressRepository.save(newAddress);
        return getAddressDto(savedAddress);
    }

    @Override
    public List<AddressDto> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(this::getAddressDto)
                .toList();
    }

    @Override
    public AddressDto getAddress(Long addressId) {
        Address address = getAddressById(addressId); // throws
        return getAddressDto(address);
    }

    @Override
    public List<AddressDto> getUserAddresses() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        List<Address> addresses = currentUser.getAddresses();
        return addresses.stream()
                .map(this::getAddressDto)
                .toList();
    }

    @Override
    @Transactional
    public AddressDto updateAddress(AddressDto addressDto) {
        Address address = getAddressById(addressDto.getAddressId());
        EcommUser currentUser = authUtil.getLoggedInUser();

        if (address.getUser() == null) {
            throw new IllegalStateException("Address is not associated with any user");
        }
        if (!address.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You cannot update this address");
        }

        address.setStreet(addressDto.getStreet());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setPincode(addressDto.getPincode());
        address.setCountry(addressDto.getCountry());

        return getAddressDto(address);
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

    private Address getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
    }

    private AddressDto getAddressDto(Address address) {
        return new AddressDto(
                address.getAddressId(),
                address.getUser().getUserId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getCountry()
        );
    }
}
