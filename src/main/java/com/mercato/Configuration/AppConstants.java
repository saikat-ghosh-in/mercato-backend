package com.mercato.Configuration;

import java.util.Set;

public class AppConstants {
    public static final String PAGE_SIZE = "200";
    public static final String SORT_CATEGORIES_BY = "categoryName";
    public static final String SORT_PRODUCTS_BY = "productName";
    public static final String SORTING_ORDER = "desc";

    public static final Set<String> ALLOWED_SORT_CATEGORY_FIELDS = Set.of(
            "categoryId",
            "categoryName",
            "createdAt"
    );
    public static final Set<String> ALLOWED_SORT_PRODUCT_FIELDS = Set.of(
            "productId",
            "productName",
            "physicalQty",
            "retailPrice",
            "discountPercent",
            "sellingPrice",
            "createdAt"
    );

    public static final String PRODUCT_IMAGE_PATH_PREFIX = "products/";
}
