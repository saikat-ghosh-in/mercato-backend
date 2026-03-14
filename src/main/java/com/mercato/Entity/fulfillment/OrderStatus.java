package com.mercato.Entity.fulfillment;

public enum OrderStatus {
    CREATED,      // Order created in system
    CONFIRMED,       // payment done, order is confirmed
    FULFILLMENT_PROCESSING,   // Vendor is processing (pick/pack)
    FULFILLMENT_COMPLETE,      // All order lines closed (fulfilled and/or cancelled)
    CANCELLED     // All order lines cancelled
}
