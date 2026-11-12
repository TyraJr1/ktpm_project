package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record WarehouseDashboardResponse(
        long totalProducts,
        long totalActiveProducts,
        long lowStockCount,
        long outOfStockCount,
        long pendingReceiptCount,
        long confirmedReceiptCountToday,
        BigDecimal confirmedImportCostToday,
        long todayTransactionCount,
        long activeSupplierCount,
        List<PurchaseReceiptSummaryResponse> recentReceipts,
        List<StockTransactionResponse> recentTransactions
) {
}
