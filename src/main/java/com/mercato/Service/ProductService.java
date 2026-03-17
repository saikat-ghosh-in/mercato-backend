package com.mercato.Service;

import com.mercato.Payloads.Request.ProductFilterRequestDTO;
import com.mercato.Payloads.Request.ProductRequestDTO;
import com.mercato.Payloads.Request.ProductSupplyUpdateRequestDTO;
import com.mercato.Payloads.Request.SellerProductFilterRequestDTO;
import com.mercato.Payloads.Response.ProductResponseDTO;
import com.mercato.Payloads.Response.ProductResponse;
import com.mercato.Payloads.Response.ProductSupplyUpdateResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    ProductResponseDTO addProduct(String categoryId, ProductRequestDTO productRequestDTO);

    ProductResponse getActiveProducts(ProductFilterRequestDTO filter);

    List<ProductResponseDTO> getSellerProducts(SellerProductFilterRequestDTO filter);

    List<ProductResponseDTO> getAllProducts();

    ProductResponseDTO getActiveProduct(String productId);

    ProductResponseDTO getSellerProduct(String productId);

    ProductResponseDTO getAdminProduct(String productId);

    ProductResponseDTO updateProduct(String productId, String categoryId, ProductRequestDTO productRequestDTO);

    void deleteProduct(String productId);

    ProductResponseDTO uploadProductImage(String productId, MultipartFile image) throws IOException;

    List<ProductSupplyUpdateResponseDTO> updateProductInventory(List<ProductSupplyUpdateRequestDTO> productSupplyUpdateRequestDTOs);
}
