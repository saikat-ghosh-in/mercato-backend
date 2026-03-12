package com.mercato.Entity.fulfillment.payment;

import com.mercato.ExceptionHandler.CustomBadRequestException;

public enum PaymentMethod {
    CARD,
    UPI,
    NET_BANKING,
    WALLET,
    CREDIT_CARD_EMI,
    DEBIT_CARD_EMI,
    CARDLESS_EMI,
    PAY_LATER,
    COD;

    public static PaymentMethod getFromString(String method) {
        try {
            return PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomBadRequestException("Invalid payment method: " + method);
        }
    }
}