package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.SalesOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    boolean existsByCreatedBy_Id(Long userId);

    boolean existsByOrderCodeIgnoreCase(String orderCode);

    @EntityGraph(attributePaths = {"customer", "createdBy"})
    @Query("select o from SalesOrder o where o.id = :id")
    Optional<SalesOrder> findDetailById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"customer", "createdBy"})
    List<SalesOrder> findByCustomer_IdOrderByOrderDateDesc(Long customerId);
}
