package com.mercato.Payloads.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CartRequestDTO {
    private List<CartItemRequestDTO> cartItems;
}