package com.ecommerce_backend.Payloads.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EcommUserDto {
    private Long id;
    private String name;
    private String email;
}
