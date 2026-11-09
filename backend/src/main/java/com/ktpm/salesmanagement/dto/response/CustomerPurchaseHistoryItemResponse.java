package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CustomerPurchaseHistoryItemResponse(
        Long orderId,
        String orderCode,
        LocalDateTime orderDate,
        Long invoiceId,
        String invoiceCode,
        BigDecimal totalAmount,
        String paymentMethod,
        String status,
        String createdBy,
        String note,
        List<InvoiceLineResponse> items
) {
}
