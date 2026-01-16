package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Cart;
import com.ecommerce_backend.Entity.CartItem;
import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.ExceptionHandler.GenericCustomException;
import com.ecommerce_backend.ExceptionHandler.ResourceAlreadyExistsException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.CartDto;
import com.ecommerce_backend.Payloads.CartItemDto;
import com.ecommerce_backend.Repository.CartItemRepository;
import com.ecommerce_backend.Repository.CartRepository;
import com.ecommerce_backend.Security.payloads.UserInfoResponse;
import com.ecommerce_backend.Security.services.AuthService;
import com.ecommerce_backend.Security.services.UserDetailsServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AuthService authService;
    private final UserDetailsServiceImpl userDetailsService;
    private final ProductService productService;

    @Override
    @Transactional
    public CartDto addProductToCart(Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart();
        Product product = productService.getProductById(productId);

        CartItem existingItem =
                cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if (existingItem != null) {
            throw new ResourceAlreadyExistsException("CartItem", "productId", productId);
        }

        if (product.getQuantity() < quantity) {
            throw new GenericCustomException(product.getProductName() + " only has " + product.getQuantity() + " units left.");
        }

        productService.updateProductInventory(productId, product.getQuantity() - quantity);

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .itemPrice(product.getSellingPrice())
                .build();

        cart.getCartItems().add(cartItem);
        cartRepository.save(cart);

        return getCartDto(cart);
    }

    @Override
    public List<CartDto> getAllCarts() {
        return cartRepository.findAll()
                .stream()
                .map(this::getCartDto)
                .collect(Collectors.toList());
    }

    @Override
    public CartDto getCartByEmail(String email) {
        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", email);
        }
        return getCartDto(cart);
    }

    @Override
    @Transactional
    public CartDto updateProductQuantityInCart(Long productId, Integer newQuantity) {

        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }

        UserInfoResponse currentUser = authService.getCurrentUserFromAuthentication();
        Cart cart = cartRepository.findCartByEmail(currentUser.getEmail());
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", currentUser.getEmail());
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "productId", productId);
        }


        int oldQuantity = cartItem.getQuantity();
        Product product = productService.getProductById(productId);

        int availableStock = product.getQuantity() + oldQuantity;
        if (availableStock < newQuantity) {
            throw new GenericCustomException(product.getProductName() + " only has " + availableStock + " units left.");
        }

        productService.updateProductInventory(productId, availableStock - newQuantity);

        if (newQuantity == 0) {
            cart.getCartItems().remove(cartItem); // orphanRemoval deletes cartItem
            if (cart.getCartItems().isEmpty()) {
                cartRepository.delete(cart);
                return null;
            }
        } else {
            cartItem.setQuantity(newQuantity);
        }

        return getCartDto(cart);
    }


    @Override
    @Transactional
    public void deleteProductFromCart(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem =
                cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "productId and cartId", productId + " >> " + cartId);
        }

        int quantityToRestore = cartItem.getQuantity();

        // orphanRemoval deletes CartItem
        cart.getCartItems().remove(cartItem);

        Product product = productService.getProductById(productId);
        productService.updateProductInventory(productId, product.getQuantity() + quantityToRestore);

        if (cart.getCartItems().isEmpty()) {
            cartRepository.delete(cart);
        }
    }

    @Override
    @Transactional
    public void deleteCart(Long cartId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            productService.updateProductInventory(product.getProductId(), product.getQuantity() + cartItem.getQuantity());
        }

        cartRepository.delete(cart); // cascade + orphanRemoval deletes the cartItems
    }


    private Cart getOrCreateCart() {
        UserInfoResponse currentUser = authService.getCurrentUserFromAuthentication();

        Cart existingCart = cartRepository.findCartByEmail(currentUser.getEmail());
        if (existingCart != null) {
            return existingCart;
        }

        Cart cart = new Cart();
        cart.setUser(userDetailsService.getUserByUsername(currentUser.getUsername()));
        return cartRepository.save(cart);
    }

    private CartDto getCartDto(Cart cart) {

        List<CartItemDto> cartItemDtoList = cart.getCartItems().stream()
                .map(cartItem -> new CartItemDto(
                        cartItem.getCartItemId(),
                        cart.getCartId(),
                        cartItem.getProduct().getProductId(),
                        cartItem.getProduct().getProductName(),
                        cartItem.getProduct().getImagePath(),
                        cartItem.getQuantity(),
                        cartItem.getItemPrice(),
                        cartItem.getLineTotal()
                )).toList();

        return new CartDto(
                cart.getCartId(),
                cart.getSubtotal(),
                cart.getDiscountPercent(),
                cart.getDiscountAmount(),
                cart.getTotalPayable(),
                cartItemDtoList
        );
    }
}
