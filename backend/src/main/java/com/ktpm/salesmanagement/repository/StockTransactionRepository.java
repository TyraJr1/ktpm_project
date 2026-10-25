package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
}

