package com.mercato.Utils;

import com.mercato.Configuration.AppConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService {

    @Override
    @Transactional
    public String uploadProductImage(String imagesFolderPath, MultipartFile productImage, String productId) throws IOException {

        String originalName = productImage.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }

        String fileName = "product-image-" + productId + extension;

        Path basePath = Paths.get(imagesFolderPath);
        Files.createDirectories(basePath);

        Path filePath = basePath.resolve(fileName); // appends fileName to the current path correctly, returns <folderPath>/fileName

        Files.copy(
                productImage.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return AppConstants.PRODUCT_IMAGE_PATH_PREFIX + fileName;
    }
}
