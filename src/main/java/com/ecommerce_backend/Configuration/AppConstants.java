package com.ecommerce_backend.Configuration;

import java.math.BigDecimal;
import java.util.Set;

public class AppConstants {
    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "50";
    public static final String SORT_CATEGORIES_BY = "categoryName";
    public static final String SORT_PRODUCTS_BY = "productName";
    public static final String SORTING_ORDER = "asc";

    public static final Set<String> ALLOWED_SORT_CATEGORY_FIELDS = Set.of(
            "categoryId",
            "categoryName"
    );
    public static final Set<String> ALLOWED_SORT_PRODUCT_FIELDS = Set.of(
            "productId",
            "productName",
            "quantity",
            "retailPrice",
            "discountPercent",
            "sellingPrice"
    );

    public static final String PRODUCT_IMAGE_PATH_PREFIX = "products/";

    public static final BigDecimal TAX_RATE = new BigDecimal("0.18"); // 18%
    public static final BigDecimal SHIPPING_FEE = new BigDecimal("50.00"); // INR
}
