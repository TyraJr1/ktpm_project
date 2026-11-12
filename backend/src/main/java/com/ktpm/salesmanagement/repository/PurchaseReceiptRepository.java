package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.PurchaseReceipt;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceipt, Long> {
    boolean existsByCreatedBy_Id(Long userId);

    boolean existsByReceiptCodeIgnoreCase(String receiptCode);

    long countByStatus(String status);

    long countByStatusAndReceiptDateBetween(String status, LocalDateTime fromDate, LocalDateTime toDate);

    @Query("""
            select coalesce(sum(pr.totalCost), 0)
            from PurchaseReceipt pr
            where pr.status = :status
              and pr.receiptDate between :fromDate and :toDate
            """)
    BigDecimal sumTotalCostByStatusAndDateRange(
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
            select pr from PurchaseReceipt pr
            join fetch pr.supplier s
            join fetch pr.createdBy u
            where pr.id = :id
            """)
    Optional<PurchaseReceipt> findDetailById(@Param("id") Long id);

    @Query(value = """
            select pr from PurchaseReceipt pr
            join fetch pr.supplier s
            join fetch pr.createdBy u
            where (:query is null
                    or lower(pr.receiptCode) like lower(concat('%', :query, '%'))
                    or lower(s.supplierCode) like lower(concat('%', :query, '%'))
                    or lower(s.supplierName) like lower(concat('%', :query, '%')))
              and (:supplierId is null or s.id = :supplierId)
              and (:status is null or pr.status = :status)
              and (:fromDate is null or pr.receiptDate >= :fromDate)
              and (:toDate is null or pr.receiptDate <= :toDate)
            """,
            countQuery = """
            select count(pr) from PurchaseReceipt pr
            join pr.supplier s
            where (:query is null
                    or lower(pr.receiptCode) like lower(concat('%', :query, '%'))
                    or lower(s.supplierCode) like lower(concat('%', :query, '%'))
                    or lower(s.supplierName) like lower(concat('%', :query, '%')))
              and (:supplierId is null or s.id = :supplierId)
              and (:status is null or pr.status = :status)
              and (:fromDate is null or pr.receiptDate >= :fromDate)
              and (:toDate is null or pr.receiptDate <= :toDate)
            """)
    Page<PurchaseReceipt> search(
            @Param("query") String query,
            @Param("supplierId") Long supplierId,
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}


