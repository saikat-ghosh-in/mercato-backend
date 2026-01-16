package com.ecommerce_backend.Controller;

import com.ecommerce_backend.Payloads.CartDto;
import com.ecommerce_backend.Repository.CartRepository;
import com.ecommerce_backend.Security.services.AuthService;
import com.ecommerce_backend.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private CartService cartService;

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDto> addProductToCart(@PathVariable Long productId,
                                                    @PathVariable Integer quantity) {
        CartDto cartDto = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<>(cartDto, HttpStatus.CREATED);
    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartDto>> getCarts() {
        List<CartDto> cartDtoList = cartService.getAllCarts();
        return new ResponseEntity<>(cartDtoList, HttpStatus.OK);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDto> getCartById() {
        String email = authService.getCurrentUserFromAuthentication().getEmail();
        CartDto cartDto = cartService.getCartByEmail(email);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    @PutMapping("/carts/products/{productId}/quantity/{newQuantity}")
    public ResponseEntity<CartDto> updateCartProduct(@PathVariable Long productId,
                                                     @PathVariable Integer newQuantity) {

        CartDto cartDto = cartService.updateProductQuantityInCart(productId, newQuantity);

        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId,
                                                        @PathVariable Long productId) {
        cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<>("Product removed from the cart.", HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}")
    public ResponseEntity<String> deleteCart(@PathVariable Long cartId) {
        cartService.deleteCart(cartId);
        return new ResponseEntity<>("Cart has been deleted.", HttpStatus.OK);
    }
}
