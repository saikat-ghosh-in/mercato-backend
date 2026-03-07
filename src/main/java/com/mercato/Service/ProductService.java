package com.mercato.Service;

import com.mercato.Entity.Product;
import com.mercato.Payloads.Request.ProductRequestDTO;
import com.mercato.Payloads.Request.ProductSupplyUpdateRequestDTO;
import com.mercato.Payloads.Response.ProductResponseDTO;
import com.mercato.Payloads.Response.ProductResponse;
import com.mercato.Payloads.Response.ProductSupplyUpdateResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    ProductResponseDTO addProduct(String categoryId, ProductRequestDTO productRequestDTO);

    ProductResponse getProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortingOrder, String categoryName, String keyword);

    ProductResponseDTO getProduct(String productId);

    ProductResponseDTO updateProduct(String productId, String categoryId, ProductRequestDTO productRequestDTO);

    void deleteProduct(String productId);

    ProductResponseDTO uploadProductImage(String productId, MultipartFile image) throws IOException;

    Product getProductByIdForUpdate(String productId);

    List<ProductSupplyUpdateResponseDTO> updateProductInventory(List<ProductSupplyUpdateRequestDTO> productSupplyUpdateRequestDTOs);

    String addDummyProducts();
}
