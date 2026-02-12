package com.ecommerce_backend.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class CustomConflictException extends BusinessException {
    public CustomConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}