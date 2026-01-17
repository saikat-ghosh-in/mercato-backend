package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressDto {
    private Long addressId;
    private Long userId;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String country;
}
