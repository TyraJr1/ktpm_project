package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.response.InvoiceDetailResponse;
import com.ktpm.salesmanagement.dto.response.InvoiceSummaryResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.service.InvoiceQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/invoices")
public class AdminInvoiceController {
    private final InvoiceQueryService service;

    public AdminInvoiceController(InvoiceQueryService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<InvoiceSummaryResponse> search(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.search(code, fromDate, toDate,
                PageRequest.of(page, size,
                        Sort.by("invoiceDate").descending()));
    }

    @GetMapping("/{id}")
    public InvoiceDetailResponse detail(@PathVariable Long id) {
        return service.getDetail(id);
    }
}
