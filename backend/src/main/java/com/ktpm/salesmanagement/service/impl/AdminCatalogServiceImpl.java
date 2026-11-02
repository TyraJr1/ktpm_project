package com.ktpm.salesmanagement.service.impl;

import com.ktpm.salesmanagement.dto.request.CategoryRequest;
import com.ktpm.salesmanagement.dto.request.ProductRequest;
import com.ktpm.salesmanagement.dto.request.SupplierRequest;
import com.ktpm.salesmanagement.dto.response.CategoryResponse;
import com.ktpm.salesmanagement.dto.response.InventoryItemResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ProductResponse;
import com.ktpm.salesmanagement.dto.response.SupplierResponse;
import com.ktpm.salesmanagement.entity.Category;
import com.ktpm.salesmanagement.entity.Product;
import com.ktpm.salesmanagement.entity.Supplier;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.exception.DuplicateResourceException;
import com.ktpm.salesmanagement.exception.ResourceNotFoundException;
import com.ktpm.salesmanagement.repository.CategoryRepository;
import com.ktpm.salesmanagement.repository.ProductRepository;
import com.ktpm.salesmanagement.repository.SupplierRepository;
import com.ktpm.salesmanagement.service.AdminCatalogService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCatalogServiceImpl implements AdminCatalogService {
    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "INACTIVE");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public AdminCatalogServiceImpl(CategoryRepository categoryRepository,
                                   ProductRepository productRepository,
                                   SupplierRepository supplierRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderByCategoryNameAsc().stream()
                .map(this::toCategoryResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = clean(request.categoryName());
        ensureCategoryNameUnique(name, null);
        Category category = new Category();
        applyCategory(category, request, name);
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục."));
        String name = clean(request.categoryName());
        ensureCategoryNameUnique(name, id);
        applyCategory(category, request, name);
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listProducts(String query, String status, Pageable pageable) {
        Page<ProductResponse> result = productRepository.search(optional(query), optionalStatus(status), pageable)
                .map(this::toProductResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String code = clean(request.productCode());
        ensureProductCodeUnique(code, null);
        Category category = activeCategory(request.categoryId());
        Product product = new Product();
        product.setStockQuantity(0);
        product.setCreatedAt(LocalDateTime.now());
        applyProduct(product, request, category, code);
        return toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm."));
        String code = clean(request.productCode());
        ensureProductCodeUnique(code, id);
        applyProduct(product, request, activeCategory(request.categoryId()), code);
        return toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProductStatus(Long id, String status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm."));
        product.setStatus(normalizeStatus(status));
        product.setUpdatedAt(LocalDateTime.now());
        return toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> listSuppliers(String query, String status, Pageable pageable) {
        return PageResponse.from(supplierRepository.search(optional(query), optionalStatus(status), pageable)
                .map(this::toSupplierResponse));
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        String code = clean(request.supplierCode());
        ensureSupplierCodeUnique(code, null);
        Supplier supplier = new Supplier();
        applySupplier(supplier, request, code);
        return toSupplierResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp."));
        String code = clean(request.supplierCode());
        ensureSupplierCodeUnique(code, id);
        applySupplier(supplier, request, code);
        return toSupplierResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryItemResponse> listInventory(String query, String status, Pageable pageable) {
        return PageResponse.from(productRepository.search(optional(query), optionalStatus(status), pageable)
                .map(product -> new InventoryItemResponse(product.getId(), product.getProductCode(),
                        product.getProductName(), product.getCategory().getCategoryName(),
                        product.getStockQuantity(), product.getStatus())));
    }

    private void applyCategory(Category category, CategoryRequest request, String name) {
        category.setCategoryName(name);
        category.setDescription(optional(request.description()));
        category.setStatus(normalizeStatus(request.status()));
    }

    private void applyProduct(Product product, ProductRequest request, Category category, String code) {
        product.setCategory(category);
        product.setProductCode(code);
        product.setProductName(clean(request.productName()));
        product.setSalePrice(request.salePrice());
        product.setStatus(normalizeStatus(request.status()));
        product.setDescription(optional(request.description()));
        product.setUpdatedAt(LocalDateTime.now());
    }

    private void applySupplier(Supplier supplier, SupplierRequest request, String code) {
        supplier.setSupplierCode(code);
        supplier.setSupplierName(clean(request.supplierName()));
        supplier.setPhone(optional(request.phone()));
        supplier.setEmail(optional(request.email()));
        supplier.setAddress(optional(request.address()));
        supplier.setStatus(normalizeStatus(request.status()));
    }

    private Category activeCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục."));
        if (!"ACTIVE".equals(category.getStatus())) {
            throw new BusinessRuleException("Danh mục được gán cho sản phẩm phải đang hoạt động.");
        }
        return category;
    }

    private void ensureCategoryNameUnique(String name, Long currentId) {
        categoryRepository.findByCategoryNameIgnoreCase(name)
                .filter(found -> !found.getId().equals(currentId))
                .ifPresent(found -> { throw new DuplicateResourceException("Tên danh mục đã tồn tại."); });
    }

    private void ensureProductCodeUnique(String code, Long currentId) {
        productRepository.findByProductCodeIgnoreCase(code)
                .filter(found -> !found.getId().equals(currentId))
                .ifPresent(found -> { throw new DuplicateResourceException("Mã sản phẩm đã tồn tại."); });
    }

    private void ensureSupplierCodeUnique(String code, Long currentId) {
        supplierRepository.findBySupplierCodeIgnoreCase(code)
                .filter(found -> !found.getId().equals(currentId))
                .ifPresent(found -> { throw new DuplicateResourceException("Mã nhà cung cấp đã tồn tại."); });
    }

    private String normalizeStatus(String value) {
        String status = clean(value).toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new BusinessRuleException("Trạng thái chỉ được là ACTIVE hoặc INACTIVE.");
        }
        return status;
    }

    private String optionalStatus(String value) {
        return optional(value) == null ? null : normalizeStatus(value);
    }

    private String optional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getCategoryName(),
                category.getDescription(), category.getStatus());
    }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getCategory().getId(),
                product.getCategory().getCategoryName(), product.getProductCode(),
                product.getProductName(), product.getSalePrice(), product.getStockQuantity(),
                product.getStatus(), product.getDescription(), product.getCreatedAt(),
                product.getUpdatedAt());
    }

    private SupplierResponse toSupplierResponse(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getSupplierCode(),
                supplier.getSupplierName(), supplier.getPhone(), supplier.getEmail(),
                supplier.getAddress(), supplier.getStatus());
    }
}
