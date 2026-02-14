package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Payloads.Request.CartItemRequestDTO;
import com.ecommerce_backend.Payloads.Request.CartRequestDTO;
import com.ecommerce_backend.Payloads.Response.CartResponseDTO;
import com.ecommerce_backend.Service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/users/cart/add")
    public ResponseEntity<CartResponseDTO> addProductToCart(@RequestBody CartRequestDTO cartRequestDTO) {
        CartResponseDTO cartResponseDTO = cartService.addProductToCart(cartRequestDTO);
        return new ResponseEntity<>(cartResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/users/cart")
    public ResponseEntity<CartResponseDTO> getCart() {
        CartResponseDTO cartResponseDTO = cartService.getCart();
        return new ResponseEntity<>(cartResponseDTO, cartResponseDTO == null ? HttpStatus.NO_CONTENT : HttpStatus.OK);
    }

    @PutMapping("/users/cart")
    public ResponseEntity<CartResponseDTO> updateProductQuantityInCart(@RequestBody CartItemRequestDTO cartItemRequestDTO) {
        CartResponseDTO cartResponseDTO = cartService.updateProductQuantityInCart(cartItemRequestDTO);
        return new ResponseEntity<>(cartResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/users/cart/remove/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long productId) {
        cartService.deleteProductFromCart(productId);
        return new ResponseEntity<>("Product removed from the cart.", HttpStatus.OK);
    }

    @DeleteMapping("/users/cart")
    public ResponseEntity<String> clearCart() {
        cartService.clearCart();
        return new ResponseEntity<>("Cart has been cleared.", HttpStatus.OK);
    }

    @GetMapping("/admin/carts")
    public ResponseEntity<List<CartResponseDTO>> getAllCarts() {
        List<CartResponseDTO> cartResponseDTOList = cartService.getAllCarts();
        return new ResponseEntity<>(cartResponseDTOList, HttpStatus.OK);
    }

    @GetMapping("/admin/carts/{cartId}")
    public ResponseEntity<CartResponseDTO> getCartById(@PathVariable Long cartId) {
        CartResponseDTO cartResponseDTO = cartService.getCartById(cartId);
        return new ResponseEntity<>(cartResponseDTO, HttpStatus.OK);
    }
}
