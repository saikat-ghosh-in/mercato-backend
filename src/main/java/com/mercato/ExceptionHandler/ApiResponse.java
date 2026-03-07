package com.mercato.ExceptionHandler;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    public String message;
    private boolean error;
}
