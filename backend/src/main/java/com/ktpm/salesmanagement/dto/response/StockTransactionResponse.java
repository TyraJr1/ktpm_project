package com.ktpm.salesmanagement.dto.response;

import java.time.LocalDateTime;

public record StockTransactionResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        String transactionType,
        Integer quantityBefore,
        Integer quantityChange,
        Integer quantityAfter,
        String reason,
        Long purchaseReceiptId,
        String receiptCode,
        Long orderId,
        String orderCode,
        String createdBy,
        LocalDateTime transactionDate
) {
}
