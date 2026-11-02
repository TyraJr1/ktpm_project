package com.ktpm.salesmanagement.service;

import com.ktpm.salesmanagement.dto.request.CategoryRequest;
import com.ktpm.salesmanagement.dto.request.ProductRequest;
import com.ktpm.salesmanagement.dto.request.SupplierRequest;
import com.ktpm.salesmanagement.dto.response.CategoryResponse;
import com.ktpm.salesmanagement.dto.response.InventoryItemResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ProductResponse;
import com.ktpm.salesmanagement.dto.response.SupplierResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface AdminCatalogService {
    List<CategoryResponse> listCategories();
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    PageResponse<ProductResponse> listProducts(String query, String status, Pageable pageable);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    ProductResponse updateProductStatus(Long id, String status);
    PageResponse<SupplierResponse> listSuppliers(String query, String status, Pageable pageable);
    SupplierResponse createSupplier(SupplierRequest request);
    SupplierResponse updateSupplier(Long id, SupplierRequest request);
    PageResponse<InventoryItemResponse> listInventory(String query, String status, Pageable pageable);
}
