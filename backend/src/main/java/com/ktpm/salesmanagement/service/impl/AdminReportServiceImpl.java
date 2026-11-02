package com.ktpm.salesmanagement.service.impl;

import com.ktpm.salesmanagement.dto.response.InventoryItemResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ReportSummaryResponse;
import com.ktpm.salesmanagement.dto.response.TopProductResponse;
import com.ktpm.salesmanagement.entity.Product;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.repository.InvoiceRepository;
import com.ktpm.salesmanagement.repository.OrderDetailRepository;
import com.ktpm.salesmanagement.repository.ProductRepository;
import com.ktpm.salesmanagement.service.AdminReportService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminReportServiceImpl implements AdminReportService {
    private static final String COMPLETED = "COMPLETED";
    private final InvoiceRepository invoiceRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    public AdminReportServiceImpl(InvoiceRepository invoiceRepository,
                                  OrderDetailRepository orderDetailRepository,
                                  ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportSummaryResponse getSummary(LocalDate fromDate, LocalDate toDate) {
        DateRange range = range(fromDate, toDate);
        return new ReportSummaryResponse(fromDate, toDate,
                invoiceRepository.sumTotalAmountByStatusAndDateRange(COMPLETED, range.from(), range.toExclusive()),
                invoiceRepository.countByStatusAndDateRange(COMPLETED, range.from(), range.toExclusive()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductResponse> getTopProducts(LocalDate fromDate, LocalDate toDate, int limit) {
        DateRange range = range(fromDate, toDate);
        if (limit < 1 || limit > 100) {
            throw new BusinessRuleException("Số sản phẩm báo cáo phải từ 1 đến 100.");
        }
        return orderDetailRepository.findTopSellingProducts(COMPLETED, range.from(), range.toExclusive(),
                        PageRequest.of(0, limit)).stream()
                .map(item -> new TopProductResponse(item.getProductId(), item.getProductCode(),
                        item.getProductName(), item.getTotalQuantity()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryItemResponse> getInventory(Pageable pageable) {
        return PageResponse.from(productRepository.findAll(pageable).map(this::toInventory));
    }

    private DateRange range(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new BusinessRuleException("Phải chọn ngày bắt đầu và ngày kết thúc.");
        }
        if (fromDate.isAfter(toDate)) {
            throw new BusinessRuleException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }
        return new DateRange(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay());
    }

    private InventoryItemResponse toInventory(Product product) {
        return new InventoryItemResponse(product.getId(), product.getProductCode(), product.getProductName(),
                product.getCategory().getCategoryName(), product.getStockQuantity(), product.getStatus());
    }

    private record DateRange(LocalDateTime from, LocalDateTime toExclusive) {
    }
}
