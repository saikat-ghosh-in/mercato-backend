package com.ecommerce_backend.Service;

import com.ecommerce_backend.Entity.EcommUser;
import com.ecommerce_backend.Entity.Product;
import com.ecommerce_backend.Payloads.Request.ProductRequestDTO;
import com.ecommerce_backend.Payloads.Request.ProductSupplyUpdateRequestDTO;
import com.ecommerce_backend.Payloads.Response.ProductResponseDTO;
import com.ecommerce_backend.Payloads.Response.ProductResponse;
import com.ecommerce_backend.Payloads.Response.ProductSupplyUpdateResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    ProductResponseDTO addProduct(Long categoryId, ProductRequestDTO productRequestDTO);

    ProductResponse getProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder, String categoryName, String keyword);

    ProductResponseDTO getProduct(Long productId);

    ProductResponseDTO updateProduct(Long productId, Long categoryId, ProductRequestDTO productRequestDTO);

    void deleteProduct(Long productId);

    ProductResponseDTO uploadProductImage(Long productId, MultipartFile image) throws IOException;

    Product getProductByIdForUpdate(Long productId);

    List<ProductSupplyUpdateResponseDTO> updateProductInventory(List<ProductSupplyUpdateRequestDTO> productSupplyUpdateRequestDTOs);

    String addDummyProducts();

    EcommUser getSeller(Product product);
}
