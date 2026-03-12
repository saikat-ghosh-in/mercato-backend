package com.mercato.Entity.fulfillment.payment;

public enum PaymentStatus {
    PENDING,
    INITIATED,
    AUTHORIZED,
    SUCCESS,
    FAILED,
    USER_DROPPED,
    CANCELLED,
    REFUND_INITIATED,
    REFUNDED,
    PARTIALLY_REFUNDED,
    REFUND_FAILED,
    REFUND_CANCELLED
}