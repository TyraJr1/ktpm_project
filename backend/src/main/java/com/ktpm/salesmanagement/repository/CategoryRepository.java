package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

