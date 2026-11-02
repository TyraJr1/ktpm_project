package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    boolean existsByCreatedBy_Id(Long userId);
}

