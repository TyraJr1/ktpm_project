package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByCategoryNameAsc();
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);
}

