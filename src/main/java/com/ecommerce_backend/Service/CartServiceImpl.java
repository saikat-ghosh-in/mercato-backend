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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    ProductService productService;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public CartDto addProductToCart(Long productId, Integer quantity) {
        Cart cart = createCart();

        Product product = productService.getProductById(productId);

        CartItem existingCartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if (existingCartItem != null) {
            throw new ResourceAlreadyExistsException("CartItem", "productId", productId);
        }
        if (product.getQuantity() == 0) {
            throw new GenericCustomException(product.getProductName() + " is out of stock.");
        }
        if (product.getQuantity() < quantity) {
            throw new GenericCustomException(product.getProductName() + " only has " + product.getQuantity() + " units left.");
        }

        int updatedInventory = product.getQuantity() - quantity;
        productService.updateProductInventory(productId, updatedInventory);

        CartItem newCartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .itemPrice(product.getSellingPrice())
                .build();
        cartItemRepository.save(newCartItem);
        cartRepository.save(cart);

        return getCartDto(cart);
    }

    @Override
    public List<CartDto> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        return carts.stream()
                .map(this::getCartDto)
                .collect(Collectors.toList());
    }

    @Override
    public CartDto getCart(String email, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(email, cartId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email and cartId", email + " >> " + cartId);
        }
        return getCartDto(cart);
    }

    @Transactional
    @Override
    public CartDto updateProductQuantityInCart(Long productId, Integer newQuantity) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Quantity must be a non-negative Integer.");
        }

        UserInfoResponse currentUser = authService.getCurrentUserFromAuthentication();

        Cart cart = cartRepository.findCartByEmail(currentUser.getEmail());
        Long cartId = cart.getCartId();

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "productId", productId);
        }
        int oldQuantity = cartItem.getQuantity();

        Product product = productService.getProductById(productId);
        if (newQuantity > oldQuantity && product.getQuantity() == 0) {
            throw new GenericCustomException(product.getProductName() + " is out of stock.");
        }
        int totalUnitsInStock = product.getQuantity() + oldQuantity;
        if (totalUnitsInStock < newQuantity) {
            throw new GenericCustomException(product.getProductName() + " only has " + totalUnitsInStock + " units left.");
        }

        int updatedInventory = totalUnitsInStock - newQuantity;
        productService.updateProductInventory(productId, updatedInventory);

        if (newQuantity == 0) {
            deleteProductFromCart(cartId, productId);
        } else {
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }

        return getCartDto(cart);
    }


    @Override
    @Transactional
    public void deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "productId and cartId", productId + " >> " + cartId);
        }

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
    }

    private Cart createCart() {
        UserInfoResponse currentUser = authService.getCurrentUserFromAuthentication();
        Cart userCart = cartRepository.findCartByEmail(currentUser.getEmail());
        if (userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        cart.setUser(userDetailsService.getUserByUsername(currentUser.getUsername()));
        return cartRepository.save(cart);
    }

    private CartDto getCartDto(Cart cart) {
        List<CartItemDto> cartItemDtoList = cart.getCartItems().stream()
                .map(cartItem -> new CartItemDto(
                        cartItem.getCartItemId(),
                        cartItem.getCart().getCartId(),
                        cartItem.getProduct().getProductId(),
                        cartItem.getProduct().getProductName(),
                        cartItem.getProduct().getImagePath(),
                        cartItem.getQuantity(),
                        cartItem.getItemPrice(),
                        cartItem.getLineTotal())
                ).toList();

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
