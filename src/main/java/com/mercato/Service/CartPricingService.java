package com.mercato.Service;

import com.mercato.Entity.cart.Cart;

public interface CartPricingService {
    void applyCharges(Cart cart);
}
