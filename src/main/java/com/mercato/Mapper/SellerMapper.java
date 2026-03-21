package com.mercato.Mapper;

import com.mercato.Entity.EcommUser;
import com.mercato.Payloads.Response.SellerResponseDTO;

public class SellerMapper {

    public static SellerResponseDTO toDto(EcommUser seller) {
        if (seller == null) return null;
        return new SellerResponseDTO(
                seller.getEmail(),
                seller.isEmailVerified(),
                seller.getCreatedAt(),
                seller.getSellerDisplayName()
        );
    }
}
