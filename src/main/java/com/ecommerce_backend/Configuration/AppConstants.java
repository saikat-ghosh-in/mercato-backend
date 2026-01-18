package com.ecommerce_backend.Configuration;

import java.math.BigDecimal;

public class AppConstants {
    public static final String PAGE_NUMBER="0";
    public static final String PAGE_SIZE="50";
    public static final String SORT_CATEGORIES_BY = "categoryId";
    public static final String SORT_PRODUCTS_BY = "productId";
    public static final String SORTING_ORDER = "asc";

    public static final BigDecimal TAX_RATE = new BigDecimal("0.18"); // 18%
    public static final BigDecimal SHIPPING_FEE = new BigDecimal("50.00"); // INR
}
