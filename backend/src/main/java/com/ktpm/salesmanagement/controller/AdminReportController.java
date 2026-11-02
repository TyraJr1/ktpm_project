package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.response.InventoryItemResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ReportSummaryResponse;
import com.ktpm.salesmanagement.dto.response.TopProductResponse;
import com.ktpm.salesmanagement.service.AdminReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {
    private final AdminReportService service;

    public AdminReportController(AdminReportService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public ReportSummaryResponse summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return service.getSummary(fromDate, toDate);
    }

    @GetMapping("/top-products")
    public List<TopProductResponse> topProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return service.getTopProducts(fromDate, toDate, limit);
    }

    @GetMapping("/inventory")
    public PageResponse<InventoryItemResponse> inventory(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "100") @Min(1) @Max(100) int size) {
        return service.getInventory(PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "productCode")));
    }
}
