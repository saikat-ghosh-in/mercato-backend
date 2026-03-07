package com.mercato.Entity.fulfillment;

public enum PaymentMethod {
    UPI,
    CARD,
    CREDIT_CARD,
    DEBIT_CARD,
    NET_BANKING,
    COD;

    public static PaymentMethod getFromString(String value) {
        return PaymentMethod.valueOf(value.trim().toUpperCase());
    }
}