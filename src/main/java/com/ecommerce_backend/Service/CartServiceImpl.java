package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Cart;
import com.ecommerce_backend.Entity.CartItem;
import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.ExceptionHandler.ResourceNotFoundException;
import com.ecommerce_backend.Payloads.Request.CartItemRequestDTO;
import com.ecommerce_backend.Payloads.Request.CartRequestDTO;
import com.ecommerce_backend.Payloads.Response.CartResponseDTO;
import com.ecommerce_backend.Payloads.Response.CartItemResponseDTO;
import com.ecommerce_backend.Repository.CartRepository;
import com.ecommerce_backend.Utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Override
    @Transactional
    public CartResponseDTO addProductToCart(CartRequestDTO cartRequestDTO) {

        Cart cart = getOrCreateCart();

        cartRequestDTO.getCartItemRequestDTOs().forEach(dto -> {

            Long productId = dto.getProductId();
            Integer requestedQuantity = dto.getQuantity();

            if (requestedQuantity == null || requestedQuantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            Product product = productService.getProductByIdForUpdate(productId);
            product.adjustInventory(-requestedQuantity);

            cart.addProduct(product, requestedQuantity);
        });

        cartRepository.save(cart);
        return buildCartResponseDTO(cart);
    }

    @Override
    public CartResponseDTO getCart() {
        return buildCartResponseDTO(getCurrentUserCart());
    }

    @Override
    public Cart getCartByUser(EcommUser user) {
        return cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user", user.getUsername()));
    }

    @Override
    @Transactional
    public CartResponseDTO updateProductQuantityInCart(CartItemRequestDTO cartItemRequestDTO) {
        Long productId = cartItemRequestDTO.getProductId();
        Integer newQuantity = cartItemRequestDTO.getQuantity();

        Product product = productService.getProductByIdForUpdate(productId);
        product.adjustInventory(-newQuantity);

        Cart cart = getCurrentUserCart();
        cart.updateProductQuantity(productId, newQuantity);

        return buildCartResponseDTO(cart);
    }

    @Override
    @Transactional
    public void deleteProductFromCart(Long productId) {
        Cart cart = getCurrentUserCart();
        Optional<CartItem> existingCartItem = cart.findItemByProductId(productId);
        if (existingCartItem.isEmpty()) {
            throw new ResourceNotFoundException("CartItem", "productId", productId);
        }
        CartItem cartItem = existingCartItem.get();

        Product product = productService.getProductByIdForUpdate(productId);
        int quantityToRestore = cartItem.getQuantity();
        product.adjustInventory(quantityToRestore);

        cart.removeCartItem(cartItem);
    }

    @Override
    @Transactional
    public void deleteCart() {
        Cart cart = getCurrentUserCart();
        cart.getCartItems().stream()
                .sorted(Comparator.comparing(cartItem -> cartItem.getProduct().getProductId()))
                .forEach(cartItem -> {
                    Long productId = cartItem.getProduct().getProductId();
                    Product product = productService.getProductByIdForUpdate(productId);
                    product.adjustInventory(cartItem.getQuantity());
                });
        cartRepository.delete(cart);
    }

    @Override
    public List<CartResponseDTO> getAllCarts() {
        return cartRepository.findAll()
                .stream()
                .map(this::buildCartResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CartResponseDTO getCartById(Long cartId) {
        if (cartId == null) {
            throw new IllegalArgumentException("cartId must not be null!");
        }
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        return buildCartResponseDTO(cart);
    }


    @Transactional
    private Cart getOrCreateCart() {
        EcommUser currentUser = authUtil.getLoggedInUser();
        Optional<Cart> existingCart = cartRepository.findByUser_UserId(currentUser.getUserId());
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

        List<CartItemResponseDTO> cartItemResponseDTOList = cart.getCartItems().stream()
                .map(cartItem -> new CartItemResponseDTO(
                        cartItem.getCartItemId(),
                        cart.getCartId(),
                        cartItem.getProduct().getProductId(),
                        cartItem.getProduct().getProductName(),
                        cartItem.getProduct().getImagePath(),
                        cartItem.getQuantity(),
                        cartItem.getItemPrice(),
                        cartItem.getLineTotal()
                )).toList();

        return new CartResponseDTO(
                cart.getCartId(),
                cart.getSubtotal(),
                cartItemResponseDTOList
        );
    }
}
