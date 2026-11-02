package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @EntityGraph(attributePaths = "product")
    List<OrderDetail> findBySalesOrderIdOrderByIdAsc(Long orderId);

    @Query("""
            select d.product.id as productId,
                   d.product.productCode as productCode,
                   d.product.productName as productName,
                   sum(d.quantity) as totalQuantity
            from OrderDetail d, Invoice i
            where i.salesOrder = d.salesOrder
              and i.status = :status
              and i.invoiceDate >= :fromDate
              and i.invoiceDate < :toDate
            group by d.product.id, d.product.productCode, d.product.productName
            order by sum(d.quantity) desc, d.product.productCode asc
            """)
    List<TopSellingProductView> findTopSellingProducts(
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
}

