package com.ecommerce_backend.Service;

import com.ecommerce_backend.Payloads.Response.EcommUserResponseDTO;

import java.util.List;

public interface UserService {
    List<EcommUserResponseDTO> getAllUsers();

    List<EcommUserResponseDTO> getAllSellers();
}
