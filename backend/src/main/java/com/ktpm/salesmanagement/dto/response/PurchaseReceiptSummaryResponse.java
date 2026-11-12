package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseReceiptSummaryResponse(
        Long id,
        String receiptCode,
        LocalDateTime receiptDate,
        Long supplierId,
        String supplierCode,
        String supplierName,
        String createdBy,
        BigDecimal totalCost,
        String status,
        String note,
        LocalDateTime createdAt
) {
}
