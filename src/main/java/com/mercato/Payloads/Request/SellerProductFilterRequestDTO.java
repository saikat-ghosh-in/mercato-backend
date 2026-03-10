package com.mercato.Payloads.Request;

import com.mercato.Configuration.AppConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerProductFilterRequestDTO {
    private String sortBy = AppConstants.SORT_PRODUCTS_BY;
    private String sortingOrder = AppConstants.SORTING_ORDER;
    private String category;
    private String keyword;
}
