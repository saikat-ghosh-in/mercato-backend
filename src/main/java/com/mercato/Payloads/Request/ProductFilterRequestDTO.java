package com.mercato.Payloads.Request;

import com.mercato.Configuration.AppConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductFilterRequestDTO {
    private Integer pageNumber = 0;
    private Integer pageSize = Integer.parseInt(AppConstants.PAGE_SIZE);
    private String sortBy = AppConstants.SORT_PRODUCTS_BY;
    private String sortingOrder = AppConstants.SORTING_ORDER;
    private String category;
    private String sellers;
    private String keyword;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean inStock;
}