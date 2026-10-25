package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

