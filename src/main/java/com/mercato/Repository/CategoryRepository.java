package com.mercato.Repository;

import com.mercato.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryId(String categoryId);

    boolean existsByCategoryName(String name);

    boolean existsByCategoryId(String categoryId);
}
