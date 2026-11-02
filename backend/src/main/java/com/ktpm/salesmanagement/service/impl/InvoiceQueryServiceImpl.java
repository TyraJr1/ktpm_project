package com.ktpm.salesmanagement.service.impl;

import com.ktpm.salesmanagement.dto.response.InvoiceDetailResponse;
import com.ktpm.salesmanagement.dto.response.InvoiceLineResponse;
import com.ktpm.salesmanagement.dto.response.InvoiceSummaryResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.entity.Invoice;
import com.ktpm.salesmanagement.entity.OrderDetail;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.exception.ResourceNotFoundException;
import com.ktpm.salesmanagement.repository.InvoiceRepository;
import com.ktpm.salesmanagement.repository.OrderDetailRepository;
import com.ktpm.salesmanagement.service.InvoiceQueryService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceQueryServiceImpl implements InvoiceQueryService {
    private final InvoiceRepository invoiceRepository;
    private final OrderDetailRepository orderDetailRepository;

    public InvoiceQueryServiceImpl(InvoiceRepository invoiceRepository,
                                   OrderDetailRepository orderDetailRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceSummaryResponse> search(String code, LocalDate fromDate,
                                                       LocalDate toDate, Pageable pageable) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessRuleException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }
        LocalDateTime from = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toExclusive = toDate == null ? null : toDate.plusDays(1).atStartOfDay();
        return PageResponse.from(invoiceRepository.search(optional(code), from, toExclusive, pageable)
                .map(this::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailResponse getDetail(Long id) {
        Invoice invoice = invoiceRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn."));
        List<InvoiceLineResponse> lines = orderDetailRepository
                .findBySalesOrderIdOrderByIdAsc(invoice.getSalesOrder().getId()).stream()
                .map(this::toLine).toList();
        String customerName = invoice.getSalesOrder().getCustomer() == null
                ? "Khách lẻ" : invoice.getSalesOrder().getCustomer().getCustomerName();
        return new InvoiceDetailResponse(invoice.getId(), invoice.getInvoiceCode(),
                invoice.getInvoiceDate(), invoice.getSalesOrder().getOrderCode(),
                invoice.getSalesOrder().getOrderDate(), customerName,
                invoice.getSalesOrder().getCreatedBy().getFullName(), invoice.getTotalAmount(),
                invoice.getPaymentMethod(), invoice.getStatus(), lines);
    }

    private InvoiceSummaryResponse toSummary(Invoice invoice) {
        return new InvoiceSummaryResponse(invoice.getId(), invoice.getInvoiceCode(),
                invoice.getInvoiceDate(), invoice.getSalesOrder().getOrderCode(),
                invoice.getTotalAmount(), invoice.getPaymentMethod(), invoice.getStatus());
    }

    private InvoiceLineResponse toLine(OrderDetail detail) {
        return new InvoiceLineResponse(detail.getProduct().getId(),
                detail.getProduct().getProductCode(), detail.getProduct().getProductName(),
                detail.getQuantity(), detail.getUnitPrice(), detail.getLineTotal());
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
