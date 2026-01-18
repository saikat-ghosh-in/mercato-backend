package com.ecommerce_backend.Entity.Fulfillment;

public enum OrderLineStatus {
    CREATED,      // Order created in system
    PLACED,       // User placed the order
    PROCESSING,   // Vendor is processing (pick/pack)
    SHIPPED,      // Dispatched
    DELIVERED,    // Delivered
    CANCELLED,    // Cancelled
    PARTIALLY_SHIPPED,    // Shipped partially
    PARTIALLY_DELIVERED,    // Delivered partially
}
