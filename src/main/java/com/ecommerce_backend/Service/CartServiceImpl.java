package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Cart;
import com.ecommerce_backend.Entity.CartItem;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.ExceptionHandler.CustomBadRequestException;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.Request.CartItemRequestDTO;
import com.ecommerce_backend.Payloads.Request.CartRequestDTO;
import com.ecommerce_backend.Payloads.Response.CartChargeResponseDTO;
import com.ecommerce_backend.Payloads.Response.CartResponseDTO;
import com.ecommerce_backend.Payloads.Response.CartItemResponseDTO;
import com.ecommerce_backend.Repository.CartRepository;
import com.ecommerce_backend.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final AuthUtil authUtil;
    private final ProductService productService;
    private final CartPricingService cartPricingService;

    @Override
    @Transactional
    public CartResponseDTO addProductToCart(CartRequestDTO cartRequestDTO) {

        Cart cart = getOrCreateCart();

        cartRequestDTO.getCartItems().forEach(dto -> {

            String productId = dto.getProductId();
            Integer requestedQuantity = dto.getQuantity();

            if (requestedQuantity == null || requestedQuantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            Product product = productService.getProductByIdForUpdate(productId);
            product.adjustInventory(-requestedQuantity);

            cart.addProduct(product, requestedQuantity);
        });

        cartPricingService.applyCharges(cart);
        cartRepository.save(cart);
        return buildCartResponseDTO(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCart() {
        Cart cart = getCurrentUserCart();
        cartPricingService.applyCharges(cart);
        return buildCartResponseDTO(cart);
    }

    @Override
    public Cart getCartByUser(EcommUser user) {
        Cart cart = cartRepository.findCartWithItemsByUserId(user.getUserId())
                .orElse(null);
        if (cart == null) return null;

        cartPricingService.applyCharges(cart);
        return cart;
    }

    @Override
    @Transactional
    public CartResponseDTO updateProductQuantityInCart(CartItemRequestDTO cartItemRequestDTO) {
        String productId = cartItemRequestDTO.getProductId();
        Integer newQuantity = cartItemRequestDTO.getQuantity();
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be less than 0");
        }

        Cart cart = getCurrentUserCart();
        if (cart == null) {
            throw new CustomBadRequestException("Cart is empty");
        }

        cart.updateProductQuantity(productId, newQuantity);
        cartPricingService.applyCharges(cart);

        cartRepository.save(cart);
        return buildCartResponseDTO(cart);
    }

    @Override
    @Transactional
    public void deleteProductFromCart(String productId) {
        Cart cart = getCurrentUserCart();
        if (cart == null) {
            throw new CustomBadRequestException("Cart is empty");
        }

        Optional<CartItem> existingCartItem = cart.findItemByProductId(productId);
        if (existingCartItem.isEmpty()) {
            throw new ResourceNotFoundException("CartItem", "productId", productId);
        }
        CartItem cartItem = existingCartItem.get();

        Product product = productService.getProductByIdForUpdate(productId);
        int quantityToRestore = cartItem.getQuantity();
        product.adjustInventory(quantityToRestore);

        cart.removeCartItem(cartItem);
        cartPricingService.applyCharges(cart);

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart() {
        Cart cart = getCurrentUserCart();
        if (cart == null) {
            throw new CustomBadRequestException("Cart does not exist");
        }

        if (cart.isEmpty()) {
            return;
        }

        List<CartItem> itemsToRestore = cart.getCartItems().stream()
                .sorted(Comparator.comparingLong(item -> item.getProduct().getId()))
                .toList();

        itemsToRestore.forEach(item -> {
            Product product = productService.getProductByIdForUpdate(item.getProduct().getProductId());
            product.adjustInventory(item.getQuantity());
        });

        cart.clear();
        cartPricingService.applyCharges(cart);

        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartResponseDTO> getAllCarts() {
        return cartRepository.findAll()
                .stream()
                .map(this::buildCartResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartById(String cartId) {
        if (cartId == null) {
            throw new IllegalArgumentException("cartId must not be null!");
        }
        Cart cart = cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        return buildCartResponseDTO(cart);
    }


    @Transactional
    private Cart getOrCreateCart() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        Optional<Cart> existingCart = cartRepository.findByUser_Id(currentUser.getId());
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        Cart cart = new Cart();
        cart.setUser(currentUser);
        return cartRepository.save(cart);
    }

    private Cart getCurrentUserCart() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        return getCartByUser(currentUser);
    }

    private CartResponseDTO buildCartResponseDTO(Cart cart) {
        if (cart == null) return null;

        List<CartItemResponseDTO> cartItemResponseDTOList = cart.getCartItems().stream()
                .map(cartItem -> new CartItemResponseDTO(
                        cartItem.getProduct().getProductId(),
                        cartItem.getQuantity(),
                        cartItem.getItemPrice(),
                        cartItem.getLineTotal()
                )).toList();

        return new CartResponseDTO(
                cart.getCartId(),
                cart.getSubtotal(),
                cart.getCharges().stream()
                        .map(charge -> new CartChargeResponseDTO(
                                charge.getType(),
                                charge.getAmount(),
                                charge.getDescription()
                        )).toList(),
                cart.getTotalCharges(),
                cart.getTotal(),
                cartItemResponseDTOList
        );
    }
}
