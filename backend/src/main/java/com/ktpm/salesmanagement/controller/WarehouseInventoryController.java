package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.request.StockAdjustmentRequest;
import com.ktpm.salesmanagement.dto.request.StockOutboundRequest;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ProductResponse;
import com.ktpm.salesmanagement.dto.response.StockTransactionResponse;
import com.ktpm.salesmanagement.dto.response.SupplierResponse;
import com.ktpm.salesmanagement.dto.response.WarehouseDashboardResponse;
import com.ktpm.salesmanagement.security.CustomUserPrincipal;
import com.ktpm.salesmanagement.service.WarehouseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/warehouse")
public class WarehouseInventoryController {

    private final WarehouseService warehouseService;

    public WarehouseInventoryController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/dashboard")
    public WarehouseDashboardResponse getDashboard() {
        return warehouseService.getDashboard();
    }

    @GetMapping("/products")
    public PageResponse<ProductResponse> listProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size
    ) {
        return warehouseService.listProducts(
                query,
                status,
                PageRequest.of(page, size, Sort.by("productName").ascending())
        );
    }

    @GetMapping("/suppliers")
    public PageResponse<SupplierResponse> listSuppliers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size
    ) {
        return warehouseService.listSuppliers(
                query,
                status,
                PageRequest.of(page, size, Sort.by("supplierName").ascending())
        );
    }

    @PostMapping("/outbound")
    @ResponseStatus(HttpStatus.CREATED)
    public StockTransactionResponse createOutbound(
            @Valid @RequestBody StockOutboundRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long staffUserId = principal != null ? principal.getUserId() : null;
        return warehouseService.createOutboundTransaction(request, staffUserId);
    }

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public StockTransactionResponse adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long staffUserId = principal != null ? principal.getUserId() : null;
        return warehouseService.adjustStock(request, staffUserId);
    }

    @GetMapping("/transactions")
    public PageResponse<StockTransactionResponse> listTransactions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size
    ) {
        return warehouseService.listStockTransactions(
                query,
                productId,
                transactionType,
                fromDate,
                toDate,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"))
        );
    }
}
