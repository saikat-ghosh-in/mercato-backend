package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.Category;
import com.ecommerce_backend.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    boolean existsByGtin(String gtin);

    Product findByGtin(String gtin);

    Page<Product> findByCategory(Pageable pageDetails, Category category);

    @Query("""
                SELECT p FROM Product p
                WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Product> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}