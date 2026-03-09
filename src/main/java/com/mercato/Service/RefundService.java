package com.mercato.Service;

import com.mercato.Entity.fulfillment.Order;

public interface RefundService {
    void processRefundIfRequired(Order order);
}
