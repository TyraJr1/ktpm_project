package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReportSummaryResponse(
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal totalRevenue,
        long invoiceCount
) {
}
