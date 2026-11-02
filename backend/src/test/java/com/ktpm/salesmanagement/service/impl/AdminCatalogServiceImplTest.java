package com.ktpm.salesmanagement.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ktpm.salesmanagement.dto.request.ProductRequest;
import com.ktpm.salesmanagement.entity.Category;
import com.ktpm.salesmanagement.entity.Product;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.repository.CategoryRepository;
import com.ktpm.salesmanagement.repository.ProductRepository;
import com.ktpm.salesmanagement.repository.SupplierRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminCatalogServiceImplTest {
    @Mock CategoryRepository categoryRepository;
    @Mock ProductRepository productRepository;
    @Mock SupplierRepository supplierRepository;

    private AdminCatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminCatalogServiceImpl(categoryRepository, productRepository, supplierRepository);
    }

    @Test
    void createProductStartsAtZeroStockAndDoesNotBypassWarehouseFlow() {
        Category category = category(10L, "Giấy", "ACTIVE");
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.findByProductCodeIgnoreCase("SP-01")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createProduct(request(10L));

        assertEquals(0, result.stockQuantity());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProductRejectsInactiveCategory() {
        when(productRepository.findByProductCodeIgnoreCase("SP-01")).thenReturn(Optional.empty());
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category(10L, "Giấy", "INACTIVE")));

        assertThrows(BusinessRuleException.class, () -> service.createProduct(request(10L)));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductStatusRejectsUnknownValue() {
        Product product = new Product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessRuleException.class, () -> service.updateProductStatus(1L, "DELETED"));

        verify(productRepository, never()).save(any(Product.class));
    }

    private ProductRequest request(Long categoryId) {
        return new ProductRequest(categoryId, " SP-01 ", "Bút bi", new BigDecimal("12000"),
                "ACTIVE", null);
    }

    private Category category(Long id, String name, String status) {
        Category category = new Category();
        category.setId(id);
        category.setCategoryName(name);
        category.setStatus(status);
        return category;
    }
}
