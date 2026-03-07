package com.mercato.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class CustomConflictException extends BusinessException {
    public CustomConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}