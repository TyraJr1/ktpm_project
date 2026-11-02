package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.response.InventoryItemResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.service.AdminCatalogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventoryController {
    private final AdminCatalogService service;

    public AdminInventoryController(AdminCatalogService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<InventoryItemResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.listInventory(query, status,
                PageRequest.of(page, size,
                        Sort.by("productName").ascending()));
    }
}
