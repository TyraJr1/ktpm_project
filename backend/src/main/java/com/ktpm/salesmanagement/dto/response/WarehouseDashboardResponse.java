package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record WarehouseDashboardResponse(
        long totalProducts,
        long lowStockCount,
        long pendingReceiptCount,
        long confirmedReceiptCountToday,
        BigDecimal confirmedImportCostToday,
        long todayTransactionCount,
        List<PurchaseReceiptSummaryResponse> recentReceipts,
        List<StockTransactionResponse> recentTransactions
) {
}
