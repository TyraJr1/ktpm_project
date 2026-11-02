package com.ktpm.salesmanagement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ktpm.salesmanagement.entity.Invoice;
import com.ktpm.salesmanagement.entity.SalesOrder;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.repository.InvoiceRepository;
import com.ktpm.salesmanagement.repository.OrderDetailRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class InvoiceQueryServiceImplTest {
    @Mock InvoiceRepository invoiceRepository;
    @Mock OrderDetailRepository orderDetailRepository;

    private InvoiceQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InvoiceQueryServiceImpl(invoiceRepository, orderDetailRepository);
    }

    @Test
    void searchRejectsReversedDateRangeBeforeRepositoryCall() {
        Pageable pageable = Pageable.unpaged();

        assertThrows(BusinessRuleException.class, () -> service.search(null,
                LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 1), pageable));

        verify(invoiceRepository, never()).search(null, null, null, pageable);
    }

    @Test
    void detailUsesRetailCustomerLabelWhenOrderHasNoCustomer() {
        User creator = new User();
        creator.setFullName("Nhân viên A");
        SalesOrder order = new SalesOrder();
        order.setId(20L);
        order.setOrderCode("DH-20");
        order.setOrderDate(LocalDateTime.of(2026, 11, 1, 8, 0));
        order.setCreatedBy(creator);
        Invoice invoice = new Invoice();
        invoice.setId(30L);
        invoice.setSalesOrder(order);
        invoice.setInvoiceCode("HD-30");
        invoice.setInvoiceDate(LocalDateTime.of(2026, 11, 1, 8, 5));
        invoice.setTotalAmount(new BigDecimal("50000"));
        invoice.setStatus("COMPLETED");
        when(invoiceRepository.findDetailById(30L)).thenReturn(Optional.of(invoice));
        when(orderDetailRepository.findBySalesOrderIdOrderByIdAsc(20L)).thenReturn(List.of());

        var result = service.getDetail(30L);

        assertEquals("Khách lẻ", result.customerName());
        assertEquals("Nhân viên A", result.createdBy());
        assertEquals(List.of(), result.lines());
    }
}
