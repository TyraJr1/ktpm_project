package com.ktpm.salesmanagement.service;

import com.ktpm.salesmanagement.dto.request.SalesOrderCreateRequest;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ProductResponse;
import com.ktpm.salesmanagement.dto.response.SalesDashboardResponse;
import com.ktpm.salesmanagement.dto.response.SalesOrderCreateResponse;
import org.springframework.data.domain.Pageable;

public interface SalesOrderService {

    PageResponse<ProductResponse> listProducts(String query, Long categoryId, String status, Pageable pageable);

    SalesOrderCreateResponse createOrderAndInvoice(SalesOrderCreateRequest request, Long staffUserId);

    SalesOrderCreateResponse getOrderDetail(Long orderId);

    SalesDashboardResponse getDashboard();
}
