package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SalesOrderCreateResponse(
        Long orderId,
        String orderCode,
        LocalDateTime orderDate,
        Long invoiceId,
        String invoiceCode,
        LocalDateTime invoiceDate,
        Long customerId,
        String customerName,
        String customerPhone,
        String createdBy,
        BigDecimal totalAmount,
        String paymentMethod,
        String status,
        String note,
        List<InvoiceLineResponse> items
) {
}
