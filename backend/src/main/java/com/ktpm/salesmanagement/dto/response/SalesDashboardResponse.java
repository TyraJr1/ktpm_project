package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record SalesDashboardResponse(
        BigDecimal todayRevenue,
        long todayInvoiceCount,
        long activeProductCount,
        long totalCustomerCount,
        List<InvoiceSummaryResponse> recentInvoices
) {
}
