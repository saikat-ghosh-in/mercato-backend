package com.mercato.Service;

import com.mercato.Entity.cart.Cart;
import com.mercato.Entity.cart.ChargeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CartPricingServiceImpl implements CartPricingService {

    @Value("${cart.shipping.threshold}")
    private BigDecimal CART_SHIPPING_THRESHOLD;
    @Value("${cart.shipping.charge}")
    private BigDecimal CART_SHIPPING_CHARGE;
    @Value("${cart.platform.fee}")
    private BigDecimal CART_PLATFORM_FEE;

    @Override
    public void applyCharges(Cart cart) {
        BigDecimal subtotal = cart.getSubtotal();
        int cartQty = cart.getCartQuantity();

        applyShipping(cart, subtotal);
        applyPlatformFee(cart);
        applyProcessing(cart, cartQty);
    }

    private void applyShipping(Cart cart, BigDecimal subtotal) {
        BigDecimal shipping = subtotal.compareTo(CART_SHIPPING_THRESHOLD) < 0
                ? CART_SHIPPING_CHARGE
                : BigDecimal.ZERO;

        cart.addOrUpdateCharge(
                ChargeType.SHIPPING,
                shipping,
                "Standard Shipping"
        );
    }

    private void applyPlatformFee(Cart cart) {
        cart.addOrUpdateCharge(
                ChargeType.PLATFORM_FEE,
                CART_PLATFORM_FEE,
                "Platform Fee"
        );
    }

    private void applyProcessing(Cart cart, int qty) {
        BigDecimal charge;
        if (qty <= 2) {
            charge = BigDecimal.ZERO;
        } else if (qty <= 10) {
            charge = BigDecimal.valueOf(25);
        } else {
            int extraQty = qty - 10;
            int blocks = (int) Math.ceil(extraQty / 5.0);
            charge = BigDecimal.valueOf(25 + (blocks * 10L));
        }

        cart.addOrUpdateCharge(
                ChargeType.PROCESSING_AND_HANDLING,
                charge,
                "Processing & Handling"
        );
    }
}
