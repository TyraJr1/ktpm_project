package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.request.CustomerRequest;
import com.ktpm.salesmanagement.dto.response.CustomerPurchaseHistoryItemResponse;
import com.ktpm.salesmanagement.dto.response.CustomerResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.service.SalesCustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
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
@RequestMapping("/api/sales/customers")
public class SalesCustomerController {

    private final SalesCustomerService salesCustomerService;

    public SalesCustomerController(SalesCustomerService salesCustomerService) {
        this.salesCustomerService = salesCustomerService;
    }

    @GetMapping
    public PageResponse<CustomerResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size
    ) {
        return salesCustomerService.listCustomers(
                query,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return salesCustomerService.getCustomer(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return salesCustomerService.createCustomer(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request
    ) {
        return salesCustomerService.updateCustomer(id, request);
    }

    @GetMapping("/{id}/purchase-history")
    public List<CustomerPurchaseHistoryItemResponse> getPurchaseHistory(@PathVariable Long id) {
        return salesCustomerService.getPurchaseHistory(id);
    }
}
