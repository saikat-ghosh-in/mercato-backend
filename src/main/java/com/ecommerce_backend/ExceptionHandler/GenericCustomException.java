package com.ecommerce_backend.ExceptionHandler;

import java.io.Serial;

public class GenericCustomException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public GenericCustomException(String message) {
        super(message);
    }
}
