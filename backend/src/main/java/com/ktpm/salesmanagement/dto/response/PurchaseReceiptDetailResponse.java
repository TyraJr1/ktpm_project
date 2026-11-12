package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseReceiptDetailResponse(
        Long id,
        String receiptCode,
        LocalDateTime receiptDate,
        Long supplierId,
        String supplierCode,
        String supplierName,
        String supplierPhone,
        String createdBy,
        BigDecimal totalCost,
        String status,
        String note,
        LocalDateTime createdAt,
        List<PurchaseReceiptLineResponse> items
) {
}
