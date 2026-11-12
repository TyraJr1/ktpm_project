package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.StockTransaction;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    boolean existsByCreatedBy_Id(Long userId);

    long countByTransactionDateBetween(LocalDateTime fromDate, LocalDateTime toDate);

    @Query(value = """
            select st from StockTransaction st
            join fetch st.product p
            join fetch st.createdBy u
            left join fetch st.purchaseReceipt pr
            left join fetch st.salesOrder so
            where (:query is null
                    or lower(p.productCode) like lower(concat('%', :query, '%'))
                    or lower(p.productName) like lower(concat('%', :query, '%'))
                    or (st.reason is not null and lower(st.reason) like lower(concat('%', :query, '%')))
                    or (pr is not null and lower(pr.receiptCode) like lower(concat('%', :query, '%')))
                    or (so is not null and lower(so.orderCode) like lower(concat('%', :query, '%'))))
              and (:productId is null or p.id = :productId)
              and (:transactionType is null or st.transactionType = :transactionType)
              and (:fromDate is null or st.transactionDate >= :fromDate)
              and (:toDate is null or st.transactionDate <= :toDate)
            """,
            countQuery = """
            select count(st) from StockTransaction st
            join st.product p
            left join st.purchaseReceipt pr
            left join st.salesOrder so
            where (:query is null
                    or lower(p.productCode) like lower(concat('%', :query, '%'))
                    or lower(p.productName) like lower(concat('%', :query, '%'))
                    or (st.reason is not null and lower(st.reason) like lower(concat('%', :query, '%')))
                    or (pr is not null and lower(pr.receiptCode) like lower(concat('%', :query, '%')))
                    or (so is not null and lower(so.orderCode) like lower(concat('%', :query, '%'))))
              and (:productId is null or p.id = :productId)
              and (:transactionType is null or st.transactionType = :transactionType)
              and (:fromDate is null or st.transactionDate >= :fromDate)
              and (:toDate is null or st.transactionDate <= :toDate)
            """)
    Page<StockTransaction> search(
            @Param("query") String query,
            @Param("productId") Long productId,
            @Param("transactionType") String transactionType,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}


