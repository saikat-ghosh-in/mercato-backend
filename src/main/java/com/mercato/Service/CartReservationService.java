package com.mercato.Service;

import com.mercato.Entity.cart.Cart;
import com.mercato.Entity.cart.CartItem;

public interface CartReservationService {

    void reserve(CartItem cartItem);

    void release(CartItem cartItem);

    void releaseAllForCart(Cart cart);

    void releaseExpired();
}

