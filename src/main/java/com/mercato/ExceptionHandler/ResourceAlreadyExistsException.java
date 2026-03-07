package com.mercato.ExceptionHandler;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends BusinessException {
    public ResourceAlreadyExistsException(String resource, String fieldName, Object fieldValue) {
        super(resource + " already exists with " + fieldName + "=" + fieldValue,
                HttpStatus.CONFLICT);
    }
}