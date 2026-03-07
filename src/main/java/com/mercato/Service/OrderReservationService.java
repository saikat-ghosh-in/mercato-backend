package com.mercato.Service;

import com.mercato.Entity.fulfillment.Order;
import com.mercato.Entity.fulfillment.OrderLine;
import com.mercato.Entity.fulfillment.OrderLineAction;

public interface OrderReservationService {
    void reserveForOrder(Order order);

    void settleQty(OrderLine orderLine, int qty, OrderLineAction action);
}