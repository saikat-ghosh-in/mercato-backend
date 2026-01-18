package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.CartDto;

import java.util.List;

public interface CartService {
    CartDto addProductToCart(Long productId, Integer quantity);

    List<CartDto> getAllCarts();

    CartDto getCartByEmail(String email);

    CartDto updateProductQuantityInCart(Long productId, Integer newQuantity);

    void deleteProductFromCart(Long cartId, Long productId);

    void deleteCart(Long cartId);
}
