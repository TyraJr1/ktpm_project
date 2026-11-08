package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    boolean existsByCreatedBy_Id(Long userId);
}

package com.yourproject.repository;

import com.yourproject.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    // Sắp xếp đơn hàng mới nhất lên đầu
    List<SalesOrder> findAllByOrderByCreatedAtDesc();
}