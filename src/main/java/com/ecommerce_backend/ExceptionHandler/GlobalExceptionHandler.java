package com.ecommerce_backend.ExceptionHandler;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Generic exception handlers
    @ExceptionHandler({Exception.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", ex.getMessage()
                ));
    }


    // Custom exception handlers
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        ApiResponse apiResponse = new ApiResponse(ex.getMessage(), true);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse apiResponse = new ApiResponse(ex.getMessage(), true);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiResponse);
    }
}

