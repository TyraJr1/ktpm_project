package com.ktpm.salesmanagement.service.impl;

import com.ktpm.salesmanagement.dto.request.SalesOrderCreateRequest;
import com.ktpm.salesmanagement.dto.request.SalesOrderItemRequest;
import com.ktpm.salesmanagement.dto.response.InvoiceLineResponse;
import com.ktpm.salesmanagement.dto.response.InvoiceSummaryResponse;
import com.ktpm.salesmanagement.dto.response.PageResponse;
import com.ktpm.salesmanagement.dto.response.ProductResponse;
import com.ktpm.salesmanagement.dto.response.SalesDashboardResponse;
import com.ktpm.salesmanagement.dto.response.SalesOrderCreateResponse;
import com.ktpm.salesmanagement.entity.Customer;
import com.ktpm.salesmanagement.entity.Invoice;
import com.ktpm.salesmanagement.entity.OrderDetail;
import com.ktpm.salesmanagement.entity.Product;
import com.ktpm.salesmanagement.entity.SalesOrder;
import com.ktpm.salesmanagement.entity.StockTransaction;
import com.ktpm.salesmanagement.entity.User;
import com.ktpm.salesmanagement.exception.BusinessRuleException;
import com.ktpm.salesmanagement.exception.ResourceNotFoundException;
import com.ktpm.salesmanagement.repository.CustomerRepository;
import com.ktpm.salesmanagement.repository.InvoiceRepository;
import com.ktpm.salesmanagement.repository.OrderDetailRepository;
import com.ktpm.salesmanagement.repository.ProductRepository;
import com.ktpm.salesmanagement.repository.SalesOrderRepository;
import com.ktpm.salesmanagement.repository.StockTransactionRepository;
import com.ktpm.salesmanagement.repository.UserRepository;
import com.ktpm.salesmanagement.service.SalesOrderService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SalesOrderServiceImpl implements SalesOrderService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String COMPLETED_STATUS = "COMPLETED";
    private static final String STOCK_TX_TYPE_SALE = "SALE";
    private static final Set<String> ALLOWED_PAYMENT_METHODS = Set.of("CASH", "TRANSFER", "CARD");
    private static final DateTimeFormatter CODE_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final InvoiceRepository invoiceRepository;
    private final StockTransactionRepository stockTransactionRepository;

    public SalesOrderServiceImpl(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            UserRepository userRepository,
            SalesOrderRepository salesOrderRepository,
            OrderDetailRepository orderDetailRepository,
            InvoiceRepository invoiceRepository,
            StockTransactionRepository stockTransactionRepository
    ) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.invoiceRepository = invoiceRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listProducts(
            String query,
            Long categoryId,
            String status,
            Pageable pageable
    ) {
        String normalizedStatus = blankToNull(status);
        return PageResponse.from(
                productRepository.search(blankToNull(query), normalizedStatus, pageable)
                        .map(this::toProductResponse)
        );
    }

    @Override
    public SalesOrderCreateResponse createOrderAndInvoice(SalesOrderCreateRequest request, Long staffUserId) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new BusinessRuleException("Đơn hàng phải có ít nhất một sản phẩm.");
        }
        if (staffUserId == null) {
            throw new BusinessRuleException("Không xác định được nhân viên thực hiện giao dịch.");
        }

        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản nhân viên bán hàng."));

        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng."));
        }

        Map<Long, Integer> aggregatedQuantities = new LinkedHashMap<>();
        for (SalesOrderItemRequest item : request.items()) {
            if (item == null || item.productId() == null) {
                throw new BusinessRuleException("Sản phẩm trong đơn hàng không hợp lệ.");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new BusinessRuleException("Số lượng mua phải lớn hơn 0.");
            }
            aggregatedQuantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        Map<Long, Product> productMap = new LinkedHashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : aggregatedQuantities.entrySet()) {
            Long productId = entry.getKey();
            int requestedQty = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm."));

            if (!ACTIVE_STATUS.equalsIgnoreCase(product.getStatus())) {
                throw new BusinessRuleException(
                        "Sản phẩm \"" + product.getProductName() + "\" hiện đang ngừng kinh doanh, không thể bán."
                );
            }

            int currentStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            if (requestedQty > currentStock) {
                throw new BusinessRuleException(
                        "Sản phẩm \"" + product.getProductName() + "\" (" + product.getProductCode()
                                + ") không đủ tồn kho (Tồn hiện tại: " + currentStock
                                + ", yêu cầu: " + requestedQty + ")."
                );
            }

            BigDecimal lineTotal = product.getSalePrice().multiply(BigDecimal.valueOf(requestedQty));
            totalAmount = totalAmount.add(lineTotal);
            productMap.put(productId, product);
        }

        LocalDateTime now = LocalDateTime.now();
        String paymentMethod = normalizePaymentMethod(request.paymentMethod());

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setOrderCode(generateUniqueOrderCode(now));
        salesOrder.setCustomer(customer);
        salesOrder.setCreatedBy(staff);
        salesOrder.setOrderDate(now);
        salesOrder.setTotalAmount(totalAmount);
        salesOrder.setStatus(COMPLETED_STATUS);
        salesOrder.setNote(blankToNull(request.note()));
        salesOrder.setCreatedAt(now);
        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);

        List<InvoiceLineResponse> lineResponses = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : aggregatedQuantities.entrySet()) {
            Product product = productMap.get(entry.getKey());
            int quantity = entry.getValue();
            BigDecimal unitPrice = product.getSalePrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            OrderDetail detail = new OrderDetail();
            detail.setSalesOrder(savedOrder);
            detail.setProduct(product);
            detail.setQuantity(quantity);
            detail.setUnitPrice(unitPrice);
            detail.setLineTotal(lineTotal);
            orderDetailRepository.save(detail);

            int stockBefore = product.getStockQuantity();
            int stockAfter = stockBefore - quantity;
            product.setStockQuantity(stockAfter);
            product.setUpdatedAt(now);
            productRepository.save(product);

            StockTransaction stockTx = new StockTransaction();
            stockTx.setProduct(product);
            stockTx.setCreatedBy(staff);
            stockTx.setPurchaseReceipt(null);
            stockTx.setSalesOrder(savedOrder);
            stockTx.setTransactionType(STOCK_TX_TYPE_SALE);
            stockTx.setQuantityChange(-quantity);
            stockTx.setQuantityBefore(stockBefore);
            stockTx.setQuantityAfter(stockAfter);
            stockTx.setReason("Bán hàng theo đơn " + savedOrder.getOrderCode());
            stockTx.setTransactionDate(now);
            stockTransactionRepository.save(stockTx);

            lineResponses.add(new InvoiceLineResponse(
                    product.getId(),
                    product.getProductCode(),
                    product.getProductName(),
                    quantity,
                    unitPrice,
                    lineTotal
            ));
        }

        Invoice invoice = new Invoice();
        invoice.setSalesOrder(savedOrder);
        invoice.setInvoiceCode(generateUniqueInvoiceCode(now));
        invoice.setInvoiceDate(now);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus(COMPLETED_STATUS);
        invoice.setCreatedAt(now);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return new SalesOrderCreateResponse(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getOrderDate(),
                savedInvoice.getId(),
                savedInvoice.getInvoiceCode(),
                savedInvoice.getInvoiceDate(),
                customer != null ? customer.getId() : null,
                customer != null ? customer.getCustomerName() : "Khách lẻ",
                customer != null ? customer.getPhone() : null,
                staff.getFullName(),
                savedOrder.getTotalAmount(),
                savedInvoice.getPaymentMethod(),
                savedOrder.getStatus(),
                savedOrder.getNote(),
                lineResponses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderCreateResponse getOrderDetail(Long orderId) {
        SalesOrder order = salesOrderRepository.findDetailById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn bán hàng."));
        Optional<Invoice> invoiceOpt = invoiceRepository.findBySalesOrder_Id(order.getId());
        List<InvoiceLineResponse> lines = orderDetailRepository.findBySalesOrderIdOrderByIdAsc(order.getId())
                .stream()
                .map(detail -> new InvoiceLineResponse(
                        detail.getProduct().getId(),
                        detail.getProduct().getProductCode(),
                        detail.getProduct().getProductName(),
                        detail.getQuantity(),
                        detail.getUnitPrice(),
                        detail.getLineTotal()
                ))
                .toList();

        Customer customer = order.getCustomer();
        return new SalesOrderCreateResponse(
                order.getId(),
                order.getOrderCode(),
                order.getOrderDate(),
                invoiceOpt.map(Invoice::getId).orElse(null),
                invoiceOpt.map(Invoice::getInvoiceCode).orElse(null),
                invoiceOpt.map(Invoice::getInvoiceDate).orElse(order.getOrderDate()),
                customer != null ? customer.getId() : null,
                customer != null ? customer.getCustomerName() : "Khách lẻ",
                customer != null ? customer.getPhone() : null,
                order.getCreatedBy() != null ? order.getCreatedBy().getFullName() : null,
                order.getTotalAmount(),
                invoiceOpt.map(Invoice::getPaymentMethod).orElse("CASH"),
                order.getStatus(),
                order.getNote(),
                lines
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SalesDashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        BigDecimal todayRevenue = invoiceRepository.sumTotalAmountByStatusAndDateRange(
                COMPLETED_STATUS, startOfToday, endOfToday
        );
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }
        long todayInvoiceCount = invoiceRepository.countByStatusAndDateRange(
                COMPLETED_STATUS, startOfToday, endOfToday
        );
        long activeProductCount = productRepository.search(
                null, ACTIVE_STATUS, PageRequest.of(0, 1)
        ).getTotalElements();
        long totalCustomerCount = customerRepository.count();

        List<InvoiceSummaryResponse> recentInvoices = invoiceRepository.search(
                        null, null, null, PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "invoiceDate"))
                )
                .map(invoice -> new InvoiceSummaryResponse(
                        invoice.getId(),
                        invoice.getInvoiceCode(),
                        invoice.getInvoiceDate(),
                        invoice.getSalesOrder().getOrderCode(),
                        invoice.getTotalAmount(),
                        invoice.getPaymentMethod(),
                        invoice.getStatus()
                ))
                .getContent();

        return new SalesDashboardResponse(
                todayRevenue,
                todayInvoiceCount,
                activeProductCount,
                totalCustomerCount,
                recentInvoices
        );
    }

    private String normalizePaymentMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            return "CASH";
        }
        String upper = raw.trim().toUpperCase(Locale.ROOT);
        if ("TIỀN MẶT".equalsIgnoreCase(raw.trim()) || "TIEN MAT".equalsIgnoreCase(raw.trim())) {
            return "CASH";
        }
        if ("CHUYỂN KHOẢN".equalsIgnoreCase(raw.trim()) || "CHUYEN KHOAN".equalsIgnoreCase(raw.trim())) {
            return "TRANSFER";
        }
        if ("THẺ".equalsIgnoreCase(raw.trim()) || "THE".equalsIgnoreCase(raw.trim())) {
            return "CARD";
        }
        if (!ALLOWED_PAYMENT_METHODS.contains(upper)) {
            throw new BusinessRuleException("Phương thức thanh toán không hợp lệ (chấp nhận: CASH, TRANSFER, CARD).");
        }
        return upper;
    }

    private String generateUniqueOrderCode(LocalDateTime now) {
        String timestamp = now.format(CODE_TIME_FMT);
        for (int attempt = 0; attempt < 10; attempt++) {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            String candidate = "ORD-" + timestamp + "-" + suffix;
            if (!salesOrderRepository.existsByOrderCodeIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return "ORD-" + System.currentTimeMillis();
    }

    private String generateUniqueInvoiceCode(LocalDateTime now) {
        String timestamp = now.format(CODE_TIME_FMT);
        for (int attempt = 0; attempt < 10; attempt++) {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            String candidate = "INV-" + timestamp + "-" + suffix;
            if (!invoiceRepository.existsByInvoiceCodeIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return "INV-" + System.currentTimeMillis();
    }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getCategoryName(),
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

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
