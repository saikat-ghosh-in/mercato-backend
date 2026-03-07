package com.mercato.Service;

import com.mercato.Payloads.Response.EcommUserResponseDTO;

import java.util.List;

public interface UserService {
    List<EcommUserResponseDTO> getAllUsers();

    List<EcommUserResponseDTO> getAllSellers();
}
