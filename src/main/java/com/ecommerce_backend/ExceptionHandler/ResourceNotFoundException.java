package com.ecommerce_backend.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, String fieldName, Object fieldValue) {
        super(resource + " does not exist with " + fieldName + "=" + fieldValue,
                HttpStatus.NOT_FOUND);
    }
}