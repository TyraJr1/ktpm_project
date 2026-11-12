package com.ktpm.salesmanagement.repository;

import com.ktpm.salesmanagement.entity.PurchaseReceiptDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseReceiptDetailRepository extends JpaRepository<PurchaseReceiptDetail, Long> {
    @Query("""
            select d from PurchaseReceiptDetail d
            join fetch d.product p
            join fetch p.category c
            where d.purchaseReceipt.id = :receiptId
            order by d.id asc
            """)
    List<PurchaseReceiptDetail> findByPurchaseReceiptIdOrderByIdAsc(@Param("receiptId") Long receiptId);
}


