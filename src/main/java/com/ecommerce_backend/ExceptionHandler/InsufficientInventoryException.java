package com.ecommerce_backend.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class InsufficientInventoryException extends BusinessException {
    public InsufficientInventoryException(String product, int availableQuantity) {
        super(product + " only has " + availableQuantity + " units left.",
                HttpStatus.PARTIAL_CONTENT
        );
    }
}