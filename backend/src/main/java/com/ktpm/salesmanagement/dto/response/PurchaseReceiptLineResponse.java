package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;

public record PurchaseReceiptLineResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        Integer currentStock,
        Integer quantity,
        BigDecimal purchasePrice,
        BigDecimal lineTotal
) {
}
