package com.ktpm.salesmanagement.service.impl;

import com.ktpm.salesmanagement.dto.request.CustomerRequest;
import com.ktpm.salesmanagement.dto.response.CustomerPurchaseHistoryItemResponse;
import com.ktpm.salesmanagement.dto.response.CustomerResponse;
import com.ktpm.salesmanagement.dto.response.InvoiceLineResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.entity.Customer;
import com.ktpm.salesmanagement.entity.Invoice;
import com.ktpm.salesmanagement.entity.SalesOrder;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.exception.ResourceNotFoundException;
import com.ktpm.salesmanagement.repository.CustomerRepository;
import com.ktpm.salesmanagement.repository.InvoiceRepository;
import com.ktpm.salesmanagement.repository.OrderDetailRepository;
import com.ktpm.salesmanagement.repository.SalesOrderRepository;
import com.ktpm.salesmanagement.service.SalesCustomerService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SalesCustomerServiceImpl implements SalesCustomerService {

    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderDetailRepository orderDetailRepository;

    public SalesCustomerServiceImpl(
            CustomerRepository customerRepository,
            SalesOrderRepository salesOrderRepository,
            InvoiceRepository invoiceRepository,
            OrderDetailRepository orderDetailRepository
    ) {
        this.customerRepository = customerRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.invoiceRepository = invoiceRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> listCustomers(String query, Pageable pageable) {
        return PageResponse.from(
                customerRepository.search(blankToNull(query), pageable).map(this::toCustomerResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        return toCustomerResponse(findCustomer(id));
    }

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        validateRequest(request);
        Customer customer = new Customer();
        applyCustomer(customer, request);
        customer.setCreatedAt(LocalDateTime.now());
        return toCustomerResponse(customerRepository.save(customer));
    }

    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        validateRequest(request);
        Customer customer = findCustomer(id);
        applyCustomer(customer, request);
        return toCustomerResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerPurchaseHistoryItemResponse> getPurchaseHistory(Long customerId) {
        findCustomer(customerId);
        List<SalesOrder> orders = salesOrderRepository.findByCustomer_IdOrderByOrderDateDesc(customerId);
        return orders.stream().map(order -> {
            Optional<Invoice> invoiceOpt = invoiceRepository.findBySalesOrder_Id(order.getId());
            List<InvoiceLineResponse> lines = orderDetailRepository.findBySalesOrderIdOrderByIdAsc(order.getId())
                    .stream()
                    .map(detail -> new InvoiceLineResponse(
                            detail.getProduct().getId(),
                            detail.getProduct().getProductCode(),
                            detail.getProduct().getProductName(),
                            detail.getQuantity(),
                            detail.getUnitPrice(),
                            detail.getLineTotal()
                    ))
                    .toList();

            return new CustomerPurchaseHistoryItemResponse(
                    order.getId(),
                    order.getOrderCode(),
                    order.getOrderDate(),
                    invoiceOpt.map(Invoice::getId).orElse(null),
                    invoiceOpt.map(Invoice::getInvoiceCode).orElse(null),
                    order.getTotalAmount(),
                    invoiceOpt.map(Invoice::getPaymentMethod).orElse("CASH"),
                    order.getStatus(),
                    order.getCreatedBy() != null ? order.getCreatedBy().getFullName() : null,
                    order.getNote(),
                    lines
            );
        }).toList();
    }

    private void validateRequest(CustomerRequest request) {
        if (request == null || request.customerName() == null || request.customerName().trim().isEmpty()) {
            throw new BusinessRuleException("Tên khách hàng không được để trống.");
        }
    }

    private void applyCustomer(Customer customer, CustomerRequest request) {
        customer.setCustomerName(request.customerName().trim());
        customer.setPhone(blankToNull(request.phone()));
        customer.setEmail(blankToNull(request.email()));
        customer.setAddress(blankToNull(request.address()));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng."));
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCreatedAt()
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
