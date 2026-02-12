package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Cart;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Payloads.Request.CartItemRequestDTO;
import com.ecommerce_backend.Payloads.Request.CartRequestDTO;
import com.ecommerce_backend.Payloads.Response.CartResponseDTO;

import java.util.List;

public interface CartService {

    CartResponseDTO addProductToCart(CartRequestDTO cartRequestDTO);

    CartResponseDTO getCart();

    Cart getCartByUser(EcommUser user);

    CartResponseDTO updateProductQuantityInCart(CartItemRequestDTO cartItemRequestDTO);

    void deleteProductFromCart(Long productId);

    void deleteCart();

    List<CartResponseDTO> getAllCarts();

    CartResponseDTO getCartById(Long cartId);
}
