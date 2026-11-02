package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;

public record InvoiceLineResponse(
        Long productId,
        String productCode,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
