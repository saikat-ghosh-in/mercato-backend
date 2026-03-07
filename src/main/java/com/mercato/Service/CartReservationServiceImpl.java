package com.mercato.Service;

import com.mercato.Entity.cart.CartReservation;
import com.mercato.Entity.Product;
import com.mercato.Entity.cart.Cart;
import com.mercato.Entity.cart.CartItem;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Repository.CartReservationRepository;
import com.mercato.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartReservationServiceImpl implements CartReservationService {

    @Value("${cart.item.reservation.minutes}")
    private int cartReservationMinutes;

    private final CartReservationRepository cartReservationRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void reserve(@NonNull CartItem cartItem) {
        Product product = getProductForUpdate(cartItem.getProduct().getProductId());
        int requestedQty = cartItem.getQuantity();

        Optional<CartReservation> existing =
                cartReservationRepository.findByCartItem_CartItemId(cartItem.getCartItemId());

        if (existing.isPresent()) {
            CartReservation cartReservation = existing.get();

            product.decreaseReservedQty(cartReservation.getReservedQty());
            product.increaseReservedQty(requestedQty);

            cartReservation.updateReservedQuantity(requestedQty);
            cartReservation.extendExpiry(cartReservationMinutes);
            productRepository.save(product);
            cartReservationRepository.save(cartReservation);

        } else {
            product.increaseReservedQty(requestedQty);
            productRepository.save(product);

            CartReservation cartReservation = CartReservation.builder()
                    .cartItem(cartItem)
                    .product(product)
                    .reservedQty(requestedQty)
                    .expiresAt(Instant.now().plusSeconds(cartReservationMinutes * 60L))
                    .build();
            cartReservationRepository.save(cartReservation);
        }
    }

    @Override
    @Transactional
    public void release(@NonNull CartItem cartItem) {
        cartReservationRepository.findByCartItem_CartItemId(cartItem.getCartItemId())
                .ifPresent(cartReservation -> {
                    Product product = getProductForUpdate(cartReservation.getProduct().getProductId());
                    product.decreaseReservedQty(cartReservation.getReservedQty());
                    productRepository.save(product);
                    cartReservationRepository.delete(cartReservation);
                });
    }

    @Override
    @Transactional
    public void releaseAllForCart(@NonNull Cart cart) {
        List<Long> cartItemIds = cart.getCartItems().stream()
                .map(CartItem::getCartItemId)
                .toList();

        if (cartItemIds.isEmpty()) return;

        List<CartReservation> cartReservations =
                cartReservationRepository.findAllByCartItemIds(cartItemIds);

        cartReservations.forEach(cartReservation -> {
            Product product = getProductForUpdate(cartReservation.getProduct().getProductId());
            product.decreaseReservedQty(cartReservation.getReservedQty());
            productRepository.save(product);
        });

        cartReservationRepository.deleteAll(cartReservations);
    }

    @Override
    @Transactional
    public void releaseExpired() {
        List<CartReservation> expired =
                cartReservationRepository.findAllExpired(Instant.now());

        if (expired.isEmpty()) return;

        for (CartReservation cartReservation : expired) {
            Product product = getProductForUpdate(cartReservation.getProduct().getProductId());
            CartItem cartItem = cartReservation.getCartItem();

            product.decreaseReservedQty(cartReservation.getReservedQty());
            productRepository.save(product);

            if (product.getAvailableQty() == 0) {
                cartItem.setOutOfStock(true);
            }

            cartReservationRepository.delete(cartReservation);
        }
    }

    private Product getProductForUpdate(String productId) {
        return productRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", productId
                ));
    }
}
