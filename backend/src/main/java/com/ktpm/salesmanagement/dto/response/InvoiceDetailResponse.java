package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceDetailResponse(
        Long id,
        String invoiceCode,
        LocalDateTime invoiceDate,
        String orderCode,
        LocalDateTime orderDate,
        String customerName,
        String createdBy,
        BigDecimal totalAmount,
        String paymentMethod,
        String status,
        List<InvoiceLineResponse> lines
) {
}
