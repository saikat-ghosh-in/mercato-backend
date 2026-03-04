package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.Cart;

public interface CartPricingService {
    void applyCharges(Cart cart);
}
