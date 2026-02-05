package com.ecommerce_backend.ExceptionHandler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Generic exception handlers
    @ExceptionHandler({Exception.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(ex.getMessage(), true));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(ex.getMessage(), true));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        String message = "Invalid request data";

        Throwable root = ex.getMostSpecificCause();
        if (root.getMessage() != null) {
            String cause = root.getMessage();
            System.err.println(cause);
            if (cause.contains("check_unique_gtin")) {
                message = "Product with the given GTIN already exists";
            } else if (cause.contains("violates not-null constraint")) {
                String marker = "violates not-null constraint";
                int index = cause.indexOf(marker);
                if (index != -1) {
                    message = cause.substring(0, index + marker.length()).trim();
                    if (message.startsWith("ERROR:")) {
                        message = message.substring(6).trim();
                    }
                }
            } else if (cause.contains("check_product_id_min_len")) {
                message = "productId must be more than 4 characters";
            } else if (cause.contains("check_name_min_len")) {
                message = "Product name must be more than 3 characters";
            } else if (cause.contains("check_description_min_len")) {
                message = "Product description must be more than 6 characters";
            } else if (cause.contains("check_unit_price_min")) {
                message = "Product unitPrice must be greater than 0";
            } else if (cause.contains("check_mark_down_range")) {
                message = "markDown must be between 0 and 99.99";
            }
        }
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse(message, true));
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

    @ExceptionHandler(GenericCustomException.class)
    public ResponseEntity<ApiResponse> handleGenericCustomException(GenericCustomException ex) {
        ApiResponse apiResponse = new ApiResponse(ex.getMessage(), false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}

