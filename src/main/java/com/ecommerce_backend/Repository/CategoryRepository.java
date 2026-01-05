package com.ecommerce_backend.Repository;

import com.ecommerce_backend.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCategoryName(String name);
    Category findByCategoryName(String name);
}
