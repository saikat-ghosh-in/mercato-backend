package com.ecommerce_backend.Entity.Fulfillment;

public enum PaymentStatus {

    INITIATED,     // Payment flow started, nothing confirmed yet
    PENDING,       // Waiting for gateway / bank confirmation
    AUTHORIZED,    // Amount authorized but not yet captured
    SUCCESS,       // Payment completed successfully
    FAILED,        // Payment failed
    CANCELLED,     // User or system cancelled before completion
    REFUND_INITIATED, // Refund process started
    REFUNDED,      // Fully refunded
    PARTIALLY_REFUNDED, // Partially refunded in case of partial cancellations
    REFUND_FAILED , // Refund failed
    REFUND_CANCELLED, // Refund cancelled before completion
}