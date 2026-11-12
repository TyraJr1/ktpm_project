package com.ktpm.salesmanagement.service;

import com.ktpm.salesmanagement.dto.request.PurchaseReceiptCreateRequest;
import com.ktpm.salesmanagement.dto.request.StockAdjustmentRequest;
import com.ktpm.salesmanagement.dto.request.StockOutboundRequest;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ProductResponse;
import com.ktpm.salesmanagement.dto.response.PurchaseReceiptDetailResponse;
import com.ktpm.salesmanagement.dto.response.PurchaseReceiptSummaryResponse;
import com.ktpm.salesmanagement.dto.response.StockTransactionResponse;
import com.ktpm.salesmanagement.dto.response.SupplierResponse;
import com.ktpm.salesmanagement.dto.response.WarehouseDashboardResponse;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {

    WarehouseDashboardResponse getDashboard();

    PageResponse<ProductResponse> listProducts(String query, String status, Pageable pageable);

    PageResponse<SupplierResponse> listSuppliers(String query, String status, Pageable pageable);

    PurchaseReceiptDetailResponse createPurchaseReceipt(PurchaseReceiptCreateRequest request, Long staffUserId);

    PurchaseReceiptDetailResponse confirmPurchaseReceipt(Long receiptId, Long staffUserId);

    PageResponse<PurchaseReceiptSummaryResponse> listPurchaseReceipts(
            String query,
            Long supplierId,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );

    PurchaseReceiptDetailResponse getPurchaseReceiptDetail(Long receiptId);

    StockTransactionResponse createOutboundTransaction(StockOutboundRequest request, Long staffUserId);

    StockTransactionResponse adjustStock(StockAdjustmentRequest request, Long staffUserId);

    PageResponse<StockTransactionResponse> listStockTransactions(
            String query,
            Long productId,
            String transactionType,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );
}
