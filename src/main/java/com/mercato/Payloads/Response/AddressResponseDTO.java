package com.mercato.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddressResponseDTO {
    private String addressId;
    private String userId;
    private String userName;
    private String recipientName;
    private String recipientPhone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
}
