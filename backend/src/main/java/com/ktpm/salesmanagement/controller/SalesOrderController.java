package com.ktpm.salesmanagement.controller;

import com.ktpm.salesmanagement.dto.request.SalesOrderCreateRequest;
import com.ktpm.salesmanagement.dto.response.SalesDashboardResponse;
import com.ktpm.salesmanagement.dto.response.SalesOrderCreateResponse;
import com.ktpm.salesmanagement.security.CustomUserPrincipal;
import com.ktpm.salesmanagement.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping("/dashboard")
    public SalesDashboardResponse getDashboard() {
        return salesOrderService.getDashboard();
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public SalesOrderCreateResponse createOrder(
            @Valid @RequestBody SalesOrderCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long staffUserId = principal != null ? principal.getUserId() : null;
        return salesOrderService.createOrderAndInvoice(request, staffUserId);
    }

    @GetMapping("/orders/{id}")
    public SalesOrderCreateResponse getOrderDetail(@PathVariable Long id) {
        return salesOrderService.getOrderDetail(id);
    }
}
