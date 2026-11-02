package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceSummaryResponse(
        Long id,
        String invoiceCode,
        LocalDateTime invoiceDate,
        String orderCode,
        BigDecimal totalAmount,
        String paymentMethod,
        String status
) {
}
