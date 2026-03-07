package com.mercato.Service;

import com.mercato.Entity.cart.Cart;
import com.mercato.Entity.cart.CartItem;
import com.mercato.Entity.EcommUser;
import com.mercato.Entity.Product;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Payloads.Request.CartItemRequestDTO;
import com.mercato.Payloads.Request.CartRequestDTO;
import com.mercato.Payloads.Response.CartChargeResponseDTO;
import com.mercato.Payloads.Response.CartResponseDTO;
import com.mercato.Payloads.Response.CartItemResponseDTO;
import com.mercato.Repository.CartRepository;
import com.mercato.Repository.UserRepository;
import com.mercato.Utils.CartContext;
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
    private final ProductService productService;
    private final CartPricingService cartPricingService;
    private final UserRepository userRepository;
    private final CartReservationService cartReservationService;

    @Override
    @Transactional
    public CartResponseDTO addProductToCart(CartRequestDTO cartRequestDTO, CartContext context) {
        Cart cart = resolveCart(context);

        cartRequestDTO.getCartItems().forEach(dto -> {
            String productId = dto.getProductId();
            Integer requestedQuantity = dto.getQuantity();
            if (requestedQuantity == null || requestedQuantity <= 0)
                throw new IllegalArgumentException("Quantity must be greater than 0");

            Product product = productService.getProductByIdForUpdate(productId);
            cart.addProduct(product, requestedQuantity);

            CartItem cartItem = cart.findItemByProductId(productId)
                    .orElseThrow(() -> new IllegalStateException("CartItem not found after add"));
            cartReservationService.reserve(cartItem);
        });

        cartPricingService.applyCharges(cart);
        cartRepository.save(cart);
        return buildCartResponseDTO(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCart(CartContext context) {
        Cart cart = resolveCart(context);
        cartPricingService.applyCharges(cart);
        return buildCartResponseDTO(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO updateProductQuantityInCart(CartItemRequestDTO dto, CartContext context) {
        if (dto.getQuantity() == null || dto.getQuantity() < 0)
            throw new IllegalArgumentException("Quantity cannot be less than 0");

        Cart cart = resolveCart(context);
        cart.updateProductQuantity(dto.getProductId(), dto.getQuantity());

        cart.findItemByProductId(dto.getProductId())
                .ifPresent(cartReservationService::reserve);

        cartPricingService.applyCharges(cart);
        cartRepository.save(cart);
        return buildCartResponseDTO(cart);
    }

    @Override
    @Transactional
    public void deleteProductFromCart(String productId, CartContext context) {
        Cart cart = resolveCart(context);
        CartItem cartItem = cart.findItemByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));

        cartReservationService.release(cartItem);
        cart.removeCartItem(cartItem);
        cartPricingService.applyCharges(cart);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(CartContext context) {
        Cart cart = resolveCart(context);
        if (cart.isEmpty()) return;

        cart.getCartItems().forEach(cartReservationService::release);
        cart.clear();
        cartPricingService.applyCharges(cart);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void mergeGuestCartOnLogin(String userId, String guestToken) {
        Optional<Cart> guestCartOpt =
                cartRepository.findCartWithItemsByGuestToken(guestToken);
        if (guestCartOpt.isEmpty()) return;

        Cart guestCart = guestCartOpt.get();
        if (guestCart.isEmpty()) {
            cartRepository.delete(guestCart);
            return;
        }

        Cart userCart = resolveCart(new CartContext(userId, null));

        for (CartItem guestItem : guestCart.getCartItems()) {
            Product product = guestItem.getProduct();
            userCart.findItemByProductId(product.getProductId())
                    .ifPresentOrElse(
                            existing -> {
                                int mergedQty = Math.max(existing.getQuantity(), guestItem.getQuantity());
                                existing.updateQuantity(mergedQty);
                                cartReservationService.reserve(existing);
                            },
                            () -> {
                                userCart.addProduct(product, guestItem.getQuantity());
                                CartItem newItem = userCart.findItemByProductId(product.getProductId())
                                        .orElseThrow();
                                cartReservationService.reserve(newItem);
                            }
                    );
        }

        cartReservationService.releaseAllForCart(guestCart);
        cartPricingService.applyCharges(userCart);
        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }

    @Override
    public Cart getCartByUser(EcommUser user) {
        Cart cart = cartRepository.findCartWithItemsByUserId(user.getUserId()).orElse(null);
        if (cart == null) return null;
        cartPricingService.applyCharges(cart);
        return cart;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartResponseDTO> getAllCarts() {
        return cartRepository.findAll().stream()
                .map(this::buildCartResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartById(String cartId) {
        if (cartId == null) throw new IllegalArgumentException("cartId must not be null!");
        Cart cart = cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        return buildCartResponseDTO(cart);
    }

    private Cart resolveCart(CartContext context) {
        if (!context.isGuest()) {
            return cartRepository.findCartWithItemsByUserId(context.userId())
                    .orElseGet(() -> createUserCart(context.userId()));
        }
        if (context.guestToken() != null) {
            return cartRepository.findCartWithItemsByGuestToken(context.guestToken())
                    .orElseGet(() -> createGuestCart(context.guestToken()));
        }
        throw new IllegalStateException("Cannot resolve cart: no userId or guestToken");
    }

    private Cart createUserCart(String userId) {
        EcommUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        Cart cart = Cart.builder()
                .user(user)
                .build();
        return cartRepository.save(cart);
    }

    private Cart createGuestCart(String guestToken) {
        Cart cart = Cart.builder()
                .guestToken(guestToken)
                .build();
        return cartRepository.save(cart);
    }

    private CartResponseDTO buildCartResponseDTO(Cart cart) {
        if (cart == null) return null;
        List<CartItemResponseDTO> items = cart.getCartItems().stream()
                .map(cartItem -> new CartItemResponseDTO(
                        cartItem.getProduct().getProductId(),
                        cartItem.getQuantity(),
                        cartItem.getItemPrice(),
                        cartItem.getLineTotal(),
                        cartItem.isOutOfStock()
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
                items
        );
    }
}