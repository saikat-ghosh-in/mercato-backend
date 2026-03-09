package com.mercato.Mapper;

import com.mercato.Entity.Address;
import com.mercato.Payloads.Response.AddressResponseDTO;

public class AddressMapper {

    public static AddressResponseDTO toDto(Address address) {
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
