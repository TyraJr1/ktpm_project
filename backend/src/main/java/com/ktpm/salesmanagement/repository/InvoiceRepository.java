package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.Invoice;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByInvoiceCodeIgnoreCase(String invoiceCode);

    @EntityGraph(attributePaths = {"salesOrder", "salesOrder.customer", "salesOrder.createdBy"})
    Optional<Invoice> findBySalesOrder_Id(Long salesOrderId);

    @EntityGraph(attributePaths = {"salesOrder", "salesOrder.customer", "salesOrder.createdBy"})
    @Query("""
            select i from Invoice i
            where i.salesOrder.customer.id = :customerId
            order by i.invoiceDate desc
            """)
    List<Invoice> findByCustomerIdOrderByInvoiceDateDesc(@Param("customerId") Long customerId);

    @EntityGraph(attributePaths = {"salesOrder", "salesOrder.customer", "salesOrder.createdBy"})
    @Query("""
            select i from Invoice i
            where (:code is null or lower(i.invoiceCode) like lower(concat('%', :code, '%'))
                    or lower(i.salesOrder.orderCode) like lower(concat('%', :code, '%')))
              and (:fromDate is null or i.invoiceDate >= :fromDate)
              and (:toDate is null or i.invoiceDate <= :toDate)
            """)
    Page<Invoice> search(@Param("code") String code, @Param("fromDate") LocalDateTime fromDate,
                         @Param("toDate") LocalDateTime toDate, Pageable pageable);

    @EntityGraph(attributePaths = {"salesOrder", "salesOrder.customer", "salesOrder.createdBy"})
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findDetailById(@Param("id") Long id);

    @Query("""
            select coalesce(sum(i.totalAmount), 0) from Invoice i
            where i.status = :status and i.invoiceDate >= :fromDate and i.invoiceDate <= :toDate
            """)
    BigDecimal sumTotalAmountByStatusAndDateRange(
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
            select count(i) from Invoice i
            where i.status = :status and i.invoiceDate >= :fromDate and i.invoiceDate <= :toDate
            """)
    long countByStatusAndDateRange(
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
