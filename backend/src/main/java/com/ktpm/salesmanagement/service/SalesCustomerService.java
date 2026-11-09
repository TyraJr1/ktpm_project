package com.ktpm.salesmanagement.service;

import com.ktpm.salesmanagement.dto.request.CustomerRequest;
import com.ktpm.salesmanagement.dto.response.CustomerPurchaseHistoryItemResponse;
import com.ktpm.salesmanagement.dto.response.CustomerResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface SalesCustomerService {

    PageResponse<CustomerResponse> listCustomers(String query, Pageable pageable);

    CustomerResponse getCustomer(Long id);

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse updateCustomer(Long id, CustomerRequest request);

    List<CustomerPurchaseHistoryItemResponse> getPurchaseHistory(Long customerId);
}
