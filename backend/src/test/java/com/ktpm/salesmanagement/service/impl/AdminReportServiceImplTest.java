package com.ktpm.salesmanagement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.repository.InvoiceRepository;
import com.ktpm.salesmanagement.repository.OrderDetailRepository;
import com.ktpm.salesmanagement.repository.ProductRepository;
import com.ktpm.salesmanagement.repository.TopSellingProductView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceImplTest {
    @Mock InvoiceRepository invoiceRepository;
    @Mock OrderDetailRepository orderDetailRepository;
    @Mock ProductRepository productRepository;
    @Mock TopSellingProductView topProduct;

    private AdminReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminReportServiceImpl(invoiceRepository, orderDetailRepository, productRepository);
    }

    @Test
    void summaryCountsOnlyCompletedInvoicesAndIncludesWholeEndDate() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 2);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime endExclusive = to.plusDays(1).atStartOfDay();
        when(invoiceRepository.sumTotalAmountByStatusAndDateRange("COMPLETED", start, endExclusive))
                .thenReturn(new BigDecimal("150000"));
        when(invoiceRepository.countByStatusAndDateRange("COMPLETED", start, endExclusive)).thenReturn(3L);

        var result = service.getSummary(from, to);

        assertEquals(new BigDecimal("150000"), result.totalRevenue());
        assertEquals(3L, result.invoiceCount());
    }

    @Test
    void summaryRejectsReversedDateRangeBeforeQuery() {
        assertThrows(BusinessRuleException.class, () -> service.getSummary(
                LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 1)));

        verify(invoiceRepository, never()).countByStatusAndDateRange(any(), any(), any());
    }

    @Test
    void topProductsMapsAggregateProjection() {
        when(topProduct.getProductId()).thenReturn(7L);
        when(topProduct.getProductCode()).thenReturn("SP-07");
        when(topProduct.getProductName()).thenReturn("Bút bi");
        when(topProduct.getTotalQuantity()).thenReturn(12L);
        when(orderDetailRepository.findTopSellingProducts(eq("COMPLETED"), any(), any(), any(Pageable.class)))
                .thenReturn(List.of(topProduct));

        var result = service.getTopProducts(LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 2), 10);

        assertEquals(1, result.size());
        assertEquals(12L, result.getFirst().totalQuantity());
    }
}
