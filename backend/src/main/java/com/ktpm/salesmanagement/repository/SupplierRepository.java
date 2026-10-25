package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}

