package com.ktpm.salesmanagement.service.impl;

import com.ktpm.salesmanagement.dto.request.PurchaseReceiptCreateRequest;
import com.ktpm.salesmanagement.dto.request.PurchaseReceiptItemRequest;
import com.ktpm.salesmanagement.dto.request.StockAdjustmentRequest;
import com.ktpm.salesmanagement.dto.request.StockOutboundRequest;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ProductResponse;
import com.ktpm.salesmanagement.dto.response.PurchaseReceiptDetailResponse;
import com.ktpm.salesmanagement.dto.response.PurchaseReceiptLineResponse;
import com.ktpm.salesmanagement.dto.response.PurchaseReceiptSummaryResponse;
import com.ktpm.salesmanagement.dto.response.StockTransactionResponse;
import com.ktpm.salesmanagement.dto.response.SupplierResponse;
import com.ktpm.salesmanagement.dto.response.WarehouseDashboardResponse;
import com.ktpm.salesmanagement.entity.Product;
import com.ktpm.salesmanagement.entity.PurchaseReceipt;
import com.ktpm.salesmanagement.entity.PurchaseReceiptDetail;
import com.ktpm.salesmanagement.entity.StockTransaction;
import com.ktpm.salesmanagement.entity.Supplier;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.exception.ResourceNotFoundException;
import com.ktpm.salesmanagement.repository.ProductRepository;
import com.ktpm.salesmanagement.repository.PurchaseReceiptDetailRepository;
import com.ktpm.salesmanagement.repository.PurchaseReceiptRepository;
import com.ktpm.salesmanagement.repository.StockTransactionRepository;
import com.ktpm.salesmanagement.repository.SupplierRepository;
import com.ktpm.salesmanagement.repository.UserRepository;
import com.ktpm.salesmanagement.service.WarehouseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String RECEIPT_STATUS_PENDING = "PENDING";
    public static final String RECEIPT_STATUS_CONFIRMED = "CONFIRMED";

    public static final String TX_TYPE_INBOUND = "INBOUND";
    public static final String TX_TYPE_OUTBOUND = "OUTBOUND";
    public static final String TX_TYPE_ADJUSTMENT = "ADJUSTMENT";
    public static final String TX_TYPE_SALE = "SALE";

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final Set<String> ALLOWED_ENTITY_STATUSES = Set.of("ACTIVE", "INACTIVE");
    private static final Set<String> ALLOWED_RECEIPT_STATUSES = Set.of(RECEIPT_STATUS_PENDING, RECEIPT_STATUS_CONFIRMED);
    private static final Set<String> ALLOWED_TX_TYPES = Set.of(
            TX_TYPE_INBOUND, TX_TYPE_OUTBOUND, TX_TYPE_ADJUSTMENT, TX_TYPE_SALE
    );
    private static final DateTimeFormatter CODE_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final PurchaseReceiptDetailRepository purchaseReceiptDetailRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public WarehouseServiceImpl(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            UserRepository userRepository,
            PurchaseReceiptRepository purchaseReceiptRepository,
            PurchaseReceiptDetailRepository purchaseReceiptDetailRepository,
            StockTransactionRepository stockTransactionRepository
    ) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.purchaseReceiptRepository = purchaseReceiptRepository;
        this.purchaseReceiptDetailRepository = purchaseReceiptDetailRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        long totalProducts = productRepository.count();
        long totalActiveProducts = productRepository.countByStatus(STATUS_ACTIVE);
        long lowStockCount = productRepository.countByStockQuantityLessThanEqual(LOW_STOCK_THRESHOLD);
        long outOfStockCount = productRepository.countByStockQuantityLessThanEqual(0);
        long pendingReceiptCount = purchaseReceiptRepository.countByStatus(RECEIPT_STATUS_PENDING);
        long confirmedReceiptCountToday = purchaseReceiptRepository.countByStatusAndReceiptDateBetween(
                RECEIPT_STATUS_CONFIRMED, startOfToday, endOfToday
        );
        BigDecimal confirmedImportCostToday = purchaseReceiptRepository.sumTotalCostByStatusAndDateRange(
                RECEIPT_STATUS_CONFIRMED, startOfToday, endOfToday
        );
        if (confirmedImportCostToday == null) {
            confirmedImportCostToday = BigDecimal.ZERO;
        }
        long todayTransactionCount = stockTransactionRepository.countByTransactionDateBetween(
                startOfToday, endOfToday
        );
        long activeSupplierCount = supplierRepository.countByStatus(STATUS_ACTIVE);

        List<PurchaseReceiptSummaryResponse> recentReceipts = purchaseReceiptRepository.search(
                        null, null, null, null, null,
                        PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "receiptDate"))
                )
                .map(this::toReceiptSummaryResponse)
                .getContent();

        List<StockTransactionResponse> recentTransactions = stockTransactionRepository.search(
                        null, null, null, null, null,
                        PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "transactionDate"))
                )
                .map(this::toStockTransactionResponse)
                .getContent();

        return new WarehouseDashboardResponse(
                totalProducts,
                totalActiveProducts,
                lowStockCount,
                outOfStockCount,
                pendingReceiptCount,
                confirmedReceiptCountToday,
                confirmedImportCostToday,
                todayTransactionCount,
                activeSupplierCount,
                recentReceipts,
                recentTransactions
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listProducts(String query, String status, Pageable pageable) {
        return PageResponse.from(
                productRepository.search(blankToNull(query), normalizeOptionalEntityStatus(status), pageable)
                        .map(this::toProductResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> listSuppliers(String query, String status, Pageable pageable) {
        return PageResponse.from(
                supplierRepository.search(blankToNull(query), normalizeOptionalEntityStatus(status), pageable)
                        .map(this::toSupplierResponse)
        );
    }

    @Override
    public PurchaseReceiptDetailResponse createPurchaseReceipt(PurchaseReceiptCreateRequest request, Long staffUserId) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new BusinessRuleException("Phiếu nhập phải có ít nhất một sản phẩm.");
        }
        if (request.supplierId() == null) {
            throw new BusinessRuleException("Phiếu nhập phải có nhà cung cấp hợp lệ.");
        }
        User staff = requireStaffUser(staffUserId);

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp."));
        if (!STATUS_ACTIVE.equalsIgnoreCase(supplier.getStatus())) {
            throw new BusinessRuleException(
                    "Nhà cung cấp \"" + supplier.getSupplierName() + "\" hiện đã ngừng hoạt động, không thể lập phiếu nhập."
            );
        }

        Set<Long> seenProductIds = new HashSet<>();
        List<Product> productsInOrder = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (PurchaseReceiptItemRequest item : request.items()) {
            if (item == null || item.productId() == null) {
                throw new BusinessRuleException("Sản phẩm trong phiếu nhập không hợp lệ.");
            }
            if (!seenProductIds.add(item.productId())) {
                throw new BusinessRuleException(
                        "Một sản phẩm không được xuất hiện nhiều lần trong cùng một phiếu nhập. Vui lòng gộp số lượng trên một dòng."
                );
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new BusinessRuleException("Số lượng nhập phải lớn hơn 0.");
            }
            if (item.purchasePrice() == null || item.purchasePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuleException("Giá nhập phải lớn hơn hoặc bằng 0.");
            }

            Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong phiếu nhập."));
            if (!STATUS_ACTIVE.equalsIgnoreCase(product.getStatus())) {
                throw new BusinessRuleException(
                        "Sản phẩm \"" + product.getProductName() + "\" hiện đã ngừng hoạt động, không thể nhập kho."
                );
            }

            BigDecimal lineTotal = item.purchasePrice().multiply(BigDecimal.valueOf(item.quantity()));
            totalCost = totalCost.add(lineTotal);
            productsInOrder.add(product);
        }

        LocalDateTime now = LocalDateTime.now();

        PurchaseReceipt receipt = new PurchaseReceipt();
        receipt.setReceiptCode(generateUniqueReceiptCode(now));
        receipt.setSupplier(supplier);
        receipt.setCreatedBy(staff);
        receipt.setReceiptDate(now);
        receipt.setTotalCost(totalCost);
        receipt.setStatus(RECEIPT_STATUS_PENDING);
        receipt.setNote(blankToNull(request.note()));
        receipt.setCreatedAt(now);
        PurchaseReceipt savedReceipt = purchaseReceiptRepository.save(receipt);

        List<PurchaseReceiptDetail> savedDetails = new ArrayList<>();
        for (int i = 0; i < request.items().size(); i++) {
            PurchaseReceiptItemRequest item = request.items().get(i);
            Product product = productsInOrder.get(i);
            BigDecimal lineTotal = item.purchasePrice().multiply(BigDecimal.valueOf(item.quantity()));

            PurchaseReceiptDetail detail = new PurchaseReceiptDetail();
            detail.setPurchaseReceipt(savedReceipt);
            detail.setProduct(product);
            detail.setQuantity(item.quantity());
            detail.setPurchasePrice(item.purchasePrice());
            detail.setLineTotal(lineTotal);
            savedDetails.add(purchaseReceiptDetailRepository.save(detail));
        }

        if (Boolean.TRUE.equals(request.confirmImmediately())) {
            applyReceiptConfirmation(savedReceipt, savedDetails, staff, now);
        }

        return buildReceiptDetailResponse(savedReceipt, savedDetails);
    }

    @Override
    public PurchaseReceiptDetailResponse confirmPurchaseReceipt(Long receiptId, Long staffUserId) {
        if (receiptId == null) {
            throw new BusinessRuleException("Mã phiếu nhập không hợp lệ.");
        }
        User staff = requireStaffUser(staffUserId);

        PurchaseReceipt receipt = purchaseReceiptRepository.findDetailById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập kho."));

        if (RECEIPT_STATUS_CONFIRMED.equalsIgnoreCase(receipt.getStatus())) {
            throw new BusinessRuleException(
                    "Phiếu nhập \"" + receipt.getReceiptCode() + "\" đã được xác nhận trước đó. Phiếu nhập chỉ được xác nhận một lần."
            );
        }
        if (!RECEIPT_STATUS_PENDING.equalsIgnoreCase(receipt.getStatus())) {
            throw new BusinessRuleException("Trạng thái phiếu nhập không hợp lệ để xác nhận.");
        }
        if (receipt.getSupplier() == null || !STATUS_ACTIVE.equalsIgnoreCase(receipt.getSupplier().getStatus())) {
            throw new BusinessRuleException("Nhà cung cấp của phiếu nhập không tồn tại hoặc đã ngừng hoạt động.");
        }

        List<PurchaseReceiptDetail> details = purchaseReceiptDetailRepository
                .findByPurchaseReceiptIdOrderByIdAsc(receipt.getId());
        if (details.isEmpty()) {
            throw new BusinessRuleException("Phiếu nhập không có sản phẩm nào, không thể xác nhận.");
        }

        LocalDateTime now = LocalDateTime.now();
        applyReceiptConfirmation(receipt, details, staff, now);
        return buildReceiptDetailResponse(receipt, details);
    }

    private void applyReceiptConfirmation(
            PurchaseReceipt receipt,
            List<PurchaseReceiptDetail> details,
            User staff,
            LocalDateTime now
    ) {
        BigDecimal totalCost = BigDecimal.ZERO;
        for (PurchaseReceiptDetail detail : details) {
            if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
                throw new BusinessRuleException("Tất cả số lượng nhập trong phiếu phải lớn hơn 0.");
            }
            Product product = detail.getProduct();
            if (product == null) {
                throw new ResourceNotFoundException("Sản phẩm trong phiếu nhập không tồn tại.");
            }
            BigDecimal lineTotal = detail.getLineTotal();
            if (lineTotal == null && detail.getPurchasePrice() != null) {
                lineTotal = detail.getPurchasePrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                detail.setLineTotal(lineTotal);
            }
            if (lineTotal != null) {
                totalCost = totalCost.add(lineTotal);
            }

            int stockBefore = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            int stockAfter = stockBefore + detail.getQuantity();

            product.setStockQuantity(stockAfter);
            product.setUpdatedAt(now);
            productRepository.save(product);

            StockTransaction stockTx = new StockTransaction();
            stockTx.setProduct(product);
            stockTx.setCreatedBy(staff);
            stockTx.setPurchaseReceipt(receipt);
            stockTx.setSalesOrder(null);
            stockTx.setTransactionType(TX_TYPE_INBOUND);
            stockTx.setQuantityBefore(stockBefore);
            stockTx.setQuantityChange(detail.getQuantity());
            stockTx.setQuantityAfter(stockAfter);
            stockTx.setReason("Nhập kho theo phiếu " + receipt.getReceiptCode());
            stockTx.setTransactionDate(now);
            stockTransactionRepository.save(stockTx);
        }

        receipt.setTotalCost(totalCost);
        receipt.setReceiptDate(now);
        receipt.setStatus(RECEIPT_STATUS_CONFIRMED);
        purchaseReceiptRepository.save(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseReceiptSummaryResponse> listPurchaseReceipts(
            String query,
            Long supplierId,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        validateDateRange(fromDate, toDate);
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        String normalizedStatus = normalizeOptionalReceiptStatus(status);

        return PageResponse.from(
                purchaseReceiptRepository.search(blankToNull(query), supplierId, normalizedStatus, start, end, pageable)
                        .map(this::toReceiptSummaryResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReceiptDetailResponse getPurchaseReceiptDetail(Long receiptId) {
        PurchaseReceipt receipt = purchaseReceiptRepository.findDetailById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập kho."));
        List<PurchaseReceiptDetail> details = purchaseReceiptDetailRepository
                .findByPurchaseReceiptIdOrderByIdAsc(receipt.getId());
        return buildReceiptDetailResponse(receipt, details);
    }

    @Override
    public StockTransactionResponse createOutboundTransaction(StockOutboundRequest request, Long staffUserId) {
        if (request == null || request.productId() == null) {
            throw new BusinessRuleException("Vui lòng chọn sản phẩm cần xuất kho.");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new BusinessRuleException("Số lượng xuất kho phải lớn hơn 0.");
        }
        String reason = blankToNull(request.reason());
        if (reason == null) {
            throw new BusinessRuleException("Vui lòng nhập lý do xuất kho rõ ràng.");
        }
        User staff = requireStaffUser(staffUserId);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm cần xuất kho."));

        int stockBefore = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int exportQty = request.quantity();
        if (exportQty > stockBefore) {
            throw new BusinessRuleException(
                    "Số lượng xuất (" + exportQty + ") vượt quá tồn kho hiện tại (" + stockBefore
                            + ") của sản phẩm \"" + product.getProductName() + "\" (" + product.getProductCode() + ")."
            );
        }

        LocalDateTime now = LocalDateTime.now();
        int stockAfter = stockBefore - exportQty;
        product.setStockQuantity(stockAfter);
        product.setUpdatedAt(now);
        productRepository.save(product);

        StockTransaction stockTx = new StockTransaction();
        stockTx.setProduct(product);
        stockTx.setCreatedBy(staff);
        stockTx.setPurchaseReceipt(null);
        stockTx.setSalesOrder(null);
        stockTx.setTransactionType(TX_TYPE_OUTBOUND);
        stockTx.setQuantityBefore(stockBefore);
        stockTx.setQuantityChange(-exportQty);
        stockTx.setQuantityAfter(stockAfter);
        stockTx.setReason(reason);
        stockTx.setTransactionDate(now);
        StockTransaction savedTx = stockTransactionRepository.save(stockTx);

        return toStockTransactionResponse(savedTx);
    }

    @Override
    public StockTransactionResponse adjustStock(StockAdjustmentRequest request, Long staffUserId) {
        if (request == null || request.productId() == null) {
            throw new BusinessRuleException("Vui lòng chọn sản phẩm cần kiểm kê / điều chỉnh.");
        }
        if (request.actualQuantity() == null || request.actualQuantity() < 0) {
            throw new BusinessRuleException("Số lượng thực tế sau kiểm kê phải là số nguyên không âm.");
        }
        String reason = blankToNull(request.reason());
        if (reason == null) {
            throw new BusinessRuleException("Chỉ được điều chỉnh tồn kho khi có lý do rõ ràng.");
        }
        User staff = requireStaffUser(staffUserId);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm cần điều chỉnh."));

        int stockBefore = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int actualQty = request.actualQuantity();
        int diff = actualQty - stockBefore;

        if (diff == 0) {
            throw new BusinessRuleException(
                    "Chênh lệch kiểm kê bằng 0 (Số lượng thực tế = Số lượng hệ thống = "
                            + stockBefore + "), không cần điều chỉnh tồn kho."
            );
        }

        LocalDateTime now = LocalDateTime.now();
        product.setStockQuantity(actualQty);
        product.setUpdatedAt(now);
        productRepository.save(product);

        StockTransaction stockTx = new StockTransaction();
        stockTx.setProduct(product);
        stockTx.setCreatedBy(staff);
        stockTx.setPurchaseReceipt(null);
        stockTx.setSalesOrder(null);
        stockTx.setTransactionType(TX_TYPE_ADJUSTMENT);
        stockTx.setQuantityBefore(stockBefore);
        stockTx.setQuantityChange(diff);
        stockTx.setQuantityAfter(actualQty);
        stockTx.setReason(reason);
        stockTx.setTransactionDate(now);
        StockTransaction savedTx = stockTransactionRepository.save(stockTx);

        return toStockTransactionResponse(savedTx);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockTransactionResponse> listStockTransactions(
            String query,
            Long productId,
            String transactionType,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        validateDateRange(fromDate, toDate);
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        String normalizedType = normalizeOptionalTransactionType(transactionType);

        return PageResponse.from(
                stockTransactionRepository.search(blankToNull(query), productId, normalizedType, start, end, pageable)
                        .map(this::toStockTransactionResponse)
        );
    }

    private User requireStaffUser(Long staffUserId) {
        if (staffUserId == null) {
            throw new BusinessRuleException("Không xác định được nhân viên kho thực hiện thao tác.");
        }
        return userRepository.findById(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản nhân viên kho."));
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessRuleException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }
    }

    private String normalizeOptionalEntityStatus(String status) {
        String cleaned = blankToNull(status);
        if (cleaned == null) return null;
        String upper = cleaned.toUpperCase(Locale.ROOT);
        if (!ALLOWED_ENTITY_STATUSES.contains(upper)) {
            throw new BusinessRuleException("Trạng thái chỉ được là ACTIVE hoặc INACTIVE.");
        }
        return upper;
    }

    private String normalizeOptionalReceiptStatus(String status) {
        String cleaned = blankToNull(status);
        if (cleaned == null) return null;
        String upper = cleaned.toUpperCase(Locale.ROOT);
        if (!ALLOWED_RECEIPT_STATUSES.contains(upper)) {
            throw new BusinessRuleException("Trạng thái phiếu nhập chỉ được là PENDING hoặc CONFIRMED.");
        }
        return upper;
    }

    private String normalizeOptionalTransactionType(String type) {
        String cleaned = blankToNull(type);
        if (cleaned == null) return null;
        String upper = cleaned.toUpperCase(Locale.ROOT);
        if (!ALLOWED_TX_TYPES.contains(upper)) {
            throw new BusinessRuleException("Loại giao dịch kho không hợp lệ (chấp nhận: INBOUND, OUTBOUND, ADJUSTMENT, SALE).");
        }
        return upper;
    }

    private String generateUniqueReceiptCode(LocalDateTime now) {
        String timestamp = now.format(CODE_TIME_FMT);
        for (int attempt = 0; attempt < 10; attempt++) {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            String candidate = "PN-" + timestamp + "-" + suffix;
            if (!purchaseReceiptRepository.existsByReceiptCodeIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return "PN-" + System.currentTimeMillis();
    }

    private PurchaseReceiptSummaryResponse toReceiptSummaryResponse(PurchaseReceipt receipt) {
        Supplier supplier = receipt.getSupplier();
        User creator = receipt.getCreatedBy();
        return new PurchaseReceiptSummaryResponse(
                receipt.getId(),
                receipt.getReceiptCode(),
                receipt.getReceiptDate(),
                supplier != null ? supplier.getId() : null,
                supplier != null ? supplier.getSupplierCode() : null,
                supplier != null ? supplier.getSupplierName() : null,
                creator != null ? creator.getFullName() : null,
                receipt.getTotalCost(),
                receipt.getStatus(),
                receipt.getNote(),
                receipt.getCreatedAt()
        );
    }

    private PurchaseReceiptDetailResponse buildReceiptDetailResponse(
            PurchaseReceipt receipt,
            List<PurchaseReceiptDetail> details
    ) {
        Supplier supplier = receipt.getSupplier();
        User creator = receipt.getCreatedBy();
        List<PurchaseReceiptLineResponse> items = details.stream()
                .map(d -> new PurchaseReceiptLineResponse(
                        d.getId(),
                        d.getProduct().getId(),
                        d.getProduct().getProductCode(),
                        d.getProduct().getProductName(),
                        d.getProduct().getStockQuantity(),
                        d.getQuantity(),
                        d.getPurchasePrice(),
                        d.getLineTotal()
                ))
                .toList();

        return new PurchaseReceiptDetailResponse(
                receipt.getId(),
                receipt.getReceiptCode(),
                receipt.getReceiptDate(),
                supplier != null ? supplier.getId() : null,
                supplier != null ? supplier.getSupplierCode() : null,
                supplier != null ? supplier.getSupplierName() : null,
                supplier != null ? supplier.getPhone() : null,
                creator != null ? creator.getFullName() : null,
                receipt.getTotalCost(),
                receipt.getStatus(),
                receipt.getNote(),
                receipt.getCreatedAt(),
                items
        );
    }

    private StockTransactionResponse toStockTransactionResponse(StockTransaction tx) {
        Product product = tx.getProduct();
        User creator = tx.getCreatedBy();
        PurchaseReceipt receipt = tx.getPurchaseReceipt();
        var order = tx.getSalesOrder();
        return new StockTransactionResponse(
                tx.getId(),
                product != null ? product.getId() : null,
                product != null ? product.getProductCode() : null,
                product != null ? product.getProductName() : null,
                tx.getTransactionType(),
                tx.getQuantityBefore(),
                tx.getQuantityChange(),
                tx.getQuantityAfter(),
                tx.getReason(),
                receipt != null ? receipt.getId() : null,
                receipt != null ? receipt.getReceiptCode() : null,
                order != null ? order.getId() : null,
                order != null ? order.getOrderCode() : null,
                creator != null ? creator.getFullName() : null,
                tx.getTransactionDate()
        );
    }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getCategoryName() : null,
                product.getProductCode(),
                product.getProductName(),
                product.getSalePrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getDescription(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private SupplierResponse toSupplierResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getSupplierCode(),
                supplier.getSupplierName(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getStatus()
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
