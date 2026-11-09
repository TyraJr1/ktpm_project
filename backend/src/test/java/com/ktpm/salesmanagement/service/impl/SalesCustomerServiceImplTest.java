package com.ktpm.salesmanagement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ktpm.salesmanagement.dto.request.CustomerRequest;
import com.ktpm.salesmanagement.entity.Category;
import com.ktpm.salesmanagement.entity.Customer;
import com.ktpm.salesmanagement.entity.Invoice;
import com.ktpm.salesmanagement.entity.OrderDetail;
import com.ktpm.salesmanagement.entity.Product;
import com.ktpm.salesmanagement.entity.SalesOrder;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.exception.ResourceNotFoundException;
import com.ktpm.salesmanagement.repository.CustomerRepository;
import com.ktpm.salesmanagement.repository.InvoiceRepository;
import com.ktpm.salesmanagement.repository.OrderDetailRepository;
import com.ktpm.salesmanagement.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalesCustomerServiceImplTest {

    @Mock CustomerRepository customerRepository;
    @Mock SalesOrderRepository salesOrderRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock OrderDetailRepository orderDetailRepository;

    private SalesCustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SalesCustomerServiceImpl(
                customerRepository,
                salesOrderRepository,
                invoiceRepository,
                orderDetailRepository
        );
    }

    @Test
    void createCustomerTrimsAndNormalizesBlankOptionalFields() {
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        var response = service.createCustomer(
                new CustomerRequest("  Nguyễn Văn A  ", "0901234567", "  ", "   ")
        );

        assertEquals(10L, response.id());
        assertEquals("Nguyễn Văn A", response.customerName());
        assertEquals("0901234567", response.phone());
        assertNull(response.email());
        assertNull(response.address());
    }

    @Test
    void createCustomerRejectsBlankName() {
        assertThrows(
                BusinessRuleException.class,
                () -> service.createCustomer(new CustomerRequest("   ", "0901234567", null, null))
        );
    }

    @Test
    void updateCustomerThrowsWhenNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateCustomer(99L, new CustomerRequest("Khách B", null, null, null))
        );
    }

    @Test
    void getPurchaseHistoryReturnsOrdersInvoicesAndLines() {
        Customer customer = new Customer();
        customer.setId(5L);
        customer.setCustomerName("Trần Thị B");
        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));

        User staff = new User();
        staff.setFullName("Nhân viên Bán Hàng");

        SalesOrder order = new SalesOrder();
        order.setId(100L);
        order.setOrderCode("ORD-100");
        order.setOrderDate(LocalDateTime.of(2026, 11, 9, 10, 0));
        order.setCustomer(customer);
        order.setCreatedBy(staff);
        order.setTotalAmount(new BigDecimal("45000"));
        order.setStatus("COMPLETED");
        when(salesOrderRepository.findByCustomer_IdOrderByOrderDateDesc(5L)).thenReturn(List.of(order));

        Invoice invoice = new Invoice();
        invoice.setId(200L);
        invoice.setInvoiceCode("INV-200");
        invoice.setPaymentMethod("TRANSFER");
        when(invoiceRepository.findBySalesOrder_Id(100L)).thenReturn(Optional.of(invoice));

        Category cat = new Category();
        cat.setId(1L);
        cat.setCategoryName("Bút viết");
        Product product = new Product();
        product.setId(7L);
        product.setCategory(cat);
        product.setProductCode("SP007");
        product.setProductName("Bút bi Thiên Long");

        OrderDetail detail = new OrderDetail();
        detail.setProduct(product);
        detail.setQuantity(3);
        detail.setUnitPrice(new BigDecimal("15000"));
        detail.setLineTotal(new BigDecimal("45000"));
        when(orderDetailRepository.findBySalesOrderIdOrderByIdAsc(100L)).thenReturn(List.of(detail));

        var history = service.getPurchaseHistory(5L);

        assertEquals(1, history.size());
        assertEquals("ORD-100", history.get(0).orderCode());
        assertEquals("INV-200", history.get(0).invoiceCode());
        assertEquals("TRANSFER", history.get(0).paymentMethod());
        assertEquals(new BigDecimal("45000"), history.get(0).totalAmount());
        assertEquals(1, history.get(0).items().size());
        assertEquals("SP007", history.get(0).items().get(0).productCode());
    }
}
