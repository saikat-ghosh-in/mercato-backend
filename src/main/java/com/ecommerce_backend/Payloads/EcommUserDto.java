package com.ecommerce_backend.Payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EcommUserDto {
    private Long id;
    private String name;
    private String email;
}
