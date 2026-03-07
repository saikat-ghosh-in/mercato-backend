package com.mercato.Entity.fulfillment;

public enum OrderStatus {
    CREATED,      // Order created in system
    CONFIRMED,       // User placed the order
    FULFILLMENT_PROCESSING,   // Vendor is processing (pick/pack)
    FULFILLMENT_COMPLETE,      // All order lines closed (delivered and/or cancelled)
    CANCELLED     // All order lines cancelled
}
