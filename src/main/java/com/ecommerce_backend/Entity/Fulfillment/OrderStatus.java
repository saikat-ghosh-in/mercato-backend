package com.ecommerce_backend.Entity.Fulfillment;

public enum OrderStatus {

    CREATED,      // Order created in system
    PLACED,       // User placed the order
    PROCESSING,   // Vendor is processing (pick/pack)
    FULFILLMENT_COMPLETE,      // All order lines closed (delivered and/or cancelled)
    CANCELLED     // All order lines cancelled
}
