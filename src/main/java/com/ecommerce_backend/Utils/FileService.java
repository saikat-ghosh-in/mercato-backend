package com.ecommerce_backend.Utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    String uploadProductImage(String imagesFolderPath, MultipartFile productImage, Long productId) throws IOException;
}
