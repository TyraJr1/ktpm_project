package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}

