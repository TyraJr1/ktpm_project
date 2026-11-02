package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.request.SupplierRequest;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.SupplierResponse;
import com.ktpm.salesmanagement.service.AdminCatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/suppliers")
public class AdminSupplierController {
    private final AdminCatalogService service;

    public AdminSupplierController(AdminCatalogService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<SupplierResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.listSuppliers(query, status,
                PageRequest.of(page, size,
                        Sort.by("supplierName").ascending()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(@Valid @RequestBody SupplierRequest request) {
        return service.createSupplier(request);
    }

    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable Long id,
                                   @Valid @RequestBody SupplierRequest request) {
        return service.updateSupplier(id, request);
    }
}
