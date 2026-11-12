package com.ktpm.salesmanagement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ktpm.salesmanagement.dto.request.PurchaseReceiptCreateRequest;
import com.ktpm.salesmanagement.dto.request.PurchaseReceiptItemRequest;
import com.ktpm.salesmanagement.dto.request.StockAdjustmentRequest;
import com.ktpm.salesmanagement.dto.request.StockOutboundRequest;
import com.ktpm.salesmanagement.entity.Category;
import com.ktpm.salesmanagement.entity.Product;
import com.ktpm.salesmanagement.entity.PurchaseReceipt;
import com.ktpm.salesmanagement.entity.PurchaseReceiptDetail;
import com.ktpm.salesmanagement.entity.StockTransaction;
import com.ktpm.salesmanagement.entity.Supplier;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.repository.ProductRepository;
import com.ktpm.salesmanagement.repository.PurchaseReceiptDetailRepository;
import com.ktpm.salesmanagement.repository.PurchaseReceiptRepository;
import com.ktpm.salesmanagement.repository.StockTransactionRepository;
import com.ktpm.salesmanagement.repository.SupplierRepository;
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
class WarehouseServiceImplTest {

    @Mock ProductRepository productRepository;
    @Mock SupplierRepository supplierRepository;
    @Mock UserRepository userRepository;
    @Mock PurchaseReceiptRepository purchaseReceiptRepository;
    @Mock PurchaseReceiptDetailRepository purchaseReceiptDetailRepository;
    @Mock StockTransactionRepository stockTransactionRepository;

    private WarehouseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WarehouseServiceImpl(
                productRepository,
                supplierRepository,
                userRepository,
                purchaseReceiptRepository,
                purchaseReceiptDetailRepository,
                stockTransactionRepository
        );
    }

    @Test
    void createPendingPurchaseReceiptDoesNotChangeStockUntilConfirmed() {
        User staff = warehouseStaff();
        Supplier supplier = activeSupplier();
        Product product = activeProduct(10L, "SP010", 20);

        when(userRepository.findById(3L)).thenReturn(Optional.of(staff));
        when(supplierRepository.findById(100L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(purchaseReceiptRepository.save(any(PurchaseReceipt.class))).thenAnswer(inv -> {
            PurchaseReceipt pr = inv.getArgument(0);
            pr.setId(900L);
            return pr;
        });
        when(purchaseReceiptDetailRepository.save(any(PurchaseReceiptDetail.class))).thenAnswer(inv -> {
            PurchaseReceiptDetail d = inv.getArgument(0);
            d.setId(901L);
            return d;
        });

        PurchaseReceiptCreateRequest request = new PurchaseReceiptCreateRequest(
                100L,
                "Nhập hàng đợt 1",
                false,
                List.of(new PurchaseReceiptItemRequest(10L, 15, new BigDecimal("12000")))
        );

        var response = service.createPurchaseReceipt(request, 3L);

        assertEquals(900L, response.id());
        assertEquals("PENDING", response.status());
        assertEquals(new BigDecimal("180000"), response.totalCost());
        assertEquals(20, product.getStockQuantity());
        verify(stockTransactionRepository, never()).save(any(StockTransaction.class));
    }

    @Test
    void confirmPurchaseReceiptIncreasesStockAndLogsInboundTransactionOnce() {
        User staff = warehouseStaff();
        Supplier supplier = activeSupplier();
        Product product = activeProduct(10L, "SP010", 20);

        PurchaseReceipt receipt = new PurchaseReceipt();
        receipt.setId(900L);
        receipt.setReceiptCode("PN-20261113-1111");
        receipt.setSupplier(supplier);
        receipt.setCreatedBy(staff);
        receipt.setStatus("PENDING");
        receipt.setTotalCost(new BigDecimal("180000"));

        PurchaseReceiptDetail detail = new PurchaseReceiptDetail();
        detail.setId(901L);
        detail.setPurchaseReceipt(receipt);
        detail.setProduct(product);
        detail.setQuantity(15);
        detail.setPurchasePrice(new BigDecimal("12000"));
        detail.setLineTotal(new BigDecimal("180000"));

        when(userRepository.findById(3L)).thenReturn(Optional.of(staff));
        when(purchaseReceiptRepository.findDetailById(900L)).thenReturn(Optional.of(receipt));
        when(purchaseReceiptDetailRepository.findByPurchaseReceiptIdOrderByIdAsc(900L))
                .thenReturn(List.of(detail));

        var response = service.confirmPurchaseReceipt(900L, 3L);

        assertEquals("CONFIRMED", response.status());
        assertEquals(35, product.getStockQuantity());

        ArgumentCaptor<StockTransaction> txCaptor = ArgumentCaptor.forClass(StockTransaction.class);
        verify(stockTransactionRepository).save(txCaptor.capture());
        StockTransaction tx = txCaptor.getValue();
        assertEquals("INBOUND", tx.getTransactionType());
        assertEquals(20, tx.getQuantityBefore());
        assertEquals(15, tx.getQuantityChange());
        assertEquals(35, tx.getQuantityAfter());
        assertEquals(receipt, tx.getPurchaseReceipt());

        assertThrows(
                BusinessRuleException.class,
                () -> service.confirmPurchaseReceipt(900L, 3L)
        );
    }

    @Test
    void outboundDeductsStockAndRejectsExceedingCurrentStock() {
        User staff = warehouseStaff();
        Product product = activeProduct(10L, "SP010", 12);

        when(userRepository.findById(3L)).thenReturn(Optional.of(staff));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(stockTransactionRepository.save(any(StockTransaction.class))).thenAnswer(inv -> {
            StockTransaction tx = inv.getArgument(0);
            tx.setId(700L);
            return tx;
        });

        var txResponse = service.createOutboundTransaction(
                new StockOutboundRequest(10L, 5, "Xuất trả hàng lỗi nhà cung cấp"),
                3L
        );

        assertEquals("OUTBOUND", txResponse.transactionType());
        assertEquals(12, txResponse.quantityBefore());
        assertEquals(-5, txResponse.quantityChange());
        assertEquals(7, txResponse.quantityAfter());
        assertEquals(7, product.getStockQuantity());

        assertThrows(
                BusinessRuleException.class,
                () -> service.createOutboundTransaction(
                        new StockOutboundRequest(10L, 8, "Xuất vượt tồn"),
                        3L
                )
        );
    }

    @Test
    void adjustStockUpdatesInventoryAndRejectsZeroDifference() {
        User staff = warehouseStaff();
        Product product = activeProduct(10L, "SP010", 10);

        when(userRepository.findById(3L)).thenReturn(Optional.of(staff));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(stockTransactionRepository.save(any(StockTransaction.class))).thenAnswer(inv -> {
            StockTransaction tx = inv.getArgument(0);
            tx.setId(800L);
            return tx;
        });

        assertThrows(
                BusinessRuleException.class,
                () -> service.adjustStock(new StockAdjustmentRequest(10L, 10, "Kiểm kê khớp"), 3L)
        );

        var adjusted = service.adjustStock(
                new StockAdjustmentRequest(10L, 8, "Kiểm kê định kỳ phát hiện thất thoát 2 cuốn"),
                3L
        );

        assertEquals("ADJUSTMENT", adjusted.transactionType());
        assertEquals(10, adjusted.quantityBefore());
        assertEquals(-2, adjusted.quantityChange());
        assertEquals(8, adjusted.quantityAfter());
        assertEquals(8, product.getStockQuantity());
    }

    @Test
    void getDashboardPopulatesAllKpisAccurateCountsAndAmounts() {
        when(productRepository.count()).thenReturn(25L);
        when(productRepository.countByStatus("ACTIVE")).thenReturn(22L);
        when(productRepository.countByStockQuantityLessThanEqual(10)).thenReturn(4L);
        when(productRepository.countByStockQuantityLessThanEqual(0)).thenReturn(1L);
        when(purchaseReceiptRepository.countByStatus("PENDING")).thenReturn(2L);
        when(purchaseReceiptRepository.countByStatusAndReceiptDateBetween(any(), any(), any())).thenReturn(3L);
        when(purchaseReceiptRepository.sumTotalCostByStatusAndDateRange(any(), any(), any()))
                .thenReturn(new BigDecimal("1450000"));
        when(stockTransactionRepository.countByTransactionDateBetween(any(), any())).thenReturn(9L);
        when(supplierRepository.countByStatus("ACTIVE")).thenReturn(6L);
        when(purchaseReceiptRepository.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));
        when(stockTransactionRepository.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        var dashboard = service.getDashboard();

        assertEquals(25L, dashboard.totalProducts());
        assertEquals(22L, dashboard.totalActiveProducts());
        assertEquals(4L, dashboard.lowStockCount());
        assertEquals(1L, dashboard.outOfStockCount());
        assertEquals(2L, dashboard.pendingReceiptCount());
        assertEquals(3L, dashboard.confirmedReceiptCountToday());
        assertEquals(new BigDecimal("1450000"), dashboard.confirmedImportCostToday());
        assertEquals(9L, dashboard.todayTransactionCount());
        assertEquals(6L, dashboard.activeSupplierCount());
    }

    private User warehouseStaff() {
        User u = new User();
        u.setId(3L);
        u.setUsername("warehouse01");
        u.setFullName("Nhân viên Kho 01");
        u.setRole("WAREHOUSE_STAFF");
        u.setStatus("ACTIVE");
        return u;
    }

    private Supplier activeSupplier() {
        Supplier s = new Supplier();
        s.setId(100L);
        s.setSupplierCode("NCC001");
        s.setSupplierName("Công ty Văn phòng phẩm Thiên Long");
        s.setStatus("ACTIVE");
        return s;
    }

    private Product activeProduct(Long id, String code, int stock) {
        Category cat = new Category();
        cat.setId(1L);
        cat.setCategoryName("Bút viết");
        cat.setStatus("ACTIVE");

        Product p = new Product();
        p.setId(id);
        p.setCategory(cat);
        p.setProductCode(code);
        p.setProductName("Bút bi Thiên Long");
        p.setSalePrice(new BigDecimal("5000"));
        p.setStockQuantity(stock);
        p.setStatus("ACTIVE");
        return p;
    }
}
