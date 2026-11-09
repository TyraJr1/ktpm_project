package com.ktpm.salesmanagement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ktpm.salesmanagement.dto.request.SalesOrderCreateRequest;
import com.ktpm.salesmanagement.dto.request.SalesOrderItemRequest;
import com.ktpm.salesmanagement.entity.Category;
import com.ktpm.salesmanagement.entity.Invoice;
import com.ktpm.salesmanagement.entity.OrderDetail;
import com.ktpm.salesmanagement.entity.Product;
import com.ktpm.salesmanagement.entity.SalesOrder;
import com.ktpm.salesmanagement.entity.StockTransaction;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.repository.CustomerRepository;
import com.ktpm.salesmanagement.repository.InvoiceRepository;
import com.ktpm.salesmanagement.repository.OrderDetailRepository;
import com.ktpm.salesmanagement.repository.ProductRepository;
import com.ktpm.salesmanagement.repository.SalesOrderRepository;
import com.ktpm.salesmanagement.repository.StockTransactionRepository;
import com.ktpm.salesmanagement.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceImplTest {

    @Mock ProductRepository productRepository;
    @Mock CustomerRepository customerRepository;
    @Mock UserRepository userRepository;
    @Mock SalesOrderRepository salesOrderRepository;
    @Mock OrderDetailRepository orderDetailRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock StockTransactionRepository stockTransactionRepository;

    private SalesOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SalesOrderServiceImpl(
                productRepository,
                customerRepository,
                userRepository,
                salesOrderRepository,
                orderDetailRepository,
                invoiceRepository,
                stockTransactionRepository
        );
    }

    @Test
    void createOrderAndInvoiceCalculatesTotalDeductsStockAndLogsStockTransaction() {
        User staff = new User();
        staff.setId(2L);
        staff.setFullName("Nhân viên Bán Hàng 01");
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));

        Category cat = new Category();
        cat.setId(1L);
        cat.setCategoryName("Sổ tập");

        Product product = new Product();
        product.setId(10L);
        product.setCategory(cat);
        product.setProductCode("SP010");
        product.setProductName("Sổ tay B5");
        product.setSalePrice(new BigDecimal("25000"));
        product.setStockQuantity(20);
        product.setStatus("ACTIVE");
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(inv -> {
            SalesOrder o = inv.getArgument(0);
            o.setId(500L);
            return o;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(600L);
            return i;
        });

        SalesOrderCreateRequest request = new SalesOrderCreateRequest(
                null,
                "CASH",
                "Khách mua lẻ tại quầy",
                List.of(
                        new SalesOrderItemRequest(10L, 2),
                        new SalesOrderItemRequest(10L, 1)
                )
        );

        var response = service.createOrderAndInvoice(request, 2L);

        assertEquals(500L, response.orderId());
        assertEquals(600L, response.invoiceId());
        assertEquals("Khách lẻ", response.customerName());
        assertEquals(new BigDecimal("75000"), response.totalAmount());
        assertEquals(1, response.items().size());
        assertEquals(3, response.items().get(0).quantity());
        assertEquals(17, product.getStockQuantity());

        ArgumentCaptor<StockTransaction> txCaptor = ArgumentCaptor.forClass(StockTransaction.class);
        verify(stockTransactionRepository).save(txCaptor.capture());
        StockTransaction savedTx = txCaptor.getValue();
        assertEquals("SALE", savedTx.getTransactionType());
        assertEquals(-3, savedTx.getQuantityChange());
        assertEquals(20, savedTx.getQuantityBefore());
        assertEquals(17, savedTx.getQuantityAfter());
        assertNotNull(savedTx.getSalesOrder());
    }

    @Test
    void createOrderRejectsWhenStockIsInsufficient() {
        User staff = new User();
        staff.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));

        Product product = new Product();
        product.setId(10L);
        product.setProductCode("SP010");
        product.setProductName("Sổ tay B5");
        product.setSalePrice(new BigDecimal("25000"));
        product.setStockQuantity(4);
        product.setStatus("ACTIVE");
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        SalesOrderCreateRequest request = new SalesOrderCreateRequest(
                null,
                "CASH",
                null,
                List.of(new SalesOrderItemRequest(10L, 5))
        );

        assertThrows(
                BusinessRuleException.class,
                () -> service.createOrderAndInvoice(request, 2L)
        );

        verify(salesOrderRepository, never()).save(any(SalesOrder.class));
        verify(orderDetailRepository, never()).save(any(OrderDetail.class));
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void createOrderRejectsInactiveProduct() {
        User staff = new User();
        staff.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));

        Product product = new Product();
        product.setId(10L);
        product.setProductCode("SP010");
        product.setProductName("Sổ tay ngừng bán");
        product.setSalePrice(new BigDecimal("25000"));
        product.setStockQuantity(50);
        product.setStatus("INACTIVE");
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        SalesOrderCreateRequest request = new SalesOrderCreateRequest(
                null,
                "CASH",
                null,
                List.of(new SalesOrderItemRequest(10L, 1))
        );

        assertThrows(
                BusinessRuleException.class,
                () -> service.createOrderAndInvoice(request, 2L)
        );
    }
}
