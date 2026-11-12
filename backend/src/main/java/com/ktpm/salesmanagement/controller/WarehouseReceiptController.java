package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.request.PurchaseReceiptCreateRequest;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.PurchaseReceiptDetailResponse;
import com.ktpm.salesmanagement.dto.response.PurchaseReceiptSummaryResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/warehouse/receipts")
public class WarehouseReceiptController {

    private final WarehouseService warehouseService;

    public WarehouseReceiptController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public PageResponse<PurchaseReceiptSummaryResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size
    ) {
        return warehouseService.listPurchaseReceipts(
                query,
                supplierId,
                status,
                fromDate,
                toDate,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "receiptDate"))
        );
    }

    @GetMapping("/{id}")
    public PurchaseReceiptDetailResponse getDetail(@PathVariable Long id) {
        return warehouseService.getPurchaseReceiptDetail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseReceiptDetailResponse createReceipt(
            @Valid @RequestBody PurchaseReceiptCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long staffUserId = principal != null ? principal.getUserId() : null;
        return warehouseService.createPurchaseReceipt(request, staffUserId);
    }

    @PostMapping("/{id}/confirm")
    public PurchaseReceiptDetailResponse confirmReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long staffUserId = principal != null ? principal.getUserId() : null;
        return warehouseService.confirmPurchaseReceipt(id, staffUserId);
    }
}
