package com.mercato.Service;

import com.mercato.Entity.Address;
import com.mercato.Entity.EcommUser;
import com.mercato.ExceptionHandler.ForbiddenOperationException;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Mapper.AddressMapper;
import com.mercato.Payloads.Request.AddressRequestDTO;
import com.mercato.Payloads.Response.AddressResponseDTO;
import com.mercato.Repository.AddressRepository;
import com.mercato.Utils.AuthUtil;
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
    @Transactional
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
        return AddressMapper.toDto(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(AddressMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDTO getAddress(String addressId) {
        Address address = getAddressById(addressId); // throws
        return AddressMapper.toDto(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getUserAddresses() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        List<Address> addresses = currentUser.getAddresses();
        return addresses.stream()
                .map(AddressMapper::toDto)
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
            throw new ForbiddenOperationException("You are not authorized to perform this action.");
        }

        address.setRecipientName(addressRequestDTO.getRecipientName());
        address.setRecipientPhone(addressRequestDTO.getRecipientPhone());
        address.setAddressLine1(addressRequestDTO.getAddressLine1());
        address.setAddressLine2(addressRequestDTO.getAddressLine2());
        address.setCity(addressRequestDTO.getCity());
        address.setState(addressRequestDTO.getState());
        address.setPincode(addressRequestDTO.getPincode());
        address.setCountry(addressRequestDTO.getCountry());

        return AddressMapper.toDto(address);
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
            throw new ForbiddenOperationException("You are not authorized to perform this action.");
        }

        currentUser.removeAddress(address);
    }


    private Address getAddressById(String addressId) {
        return addressRepository.findByAddressId(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
    }
}
