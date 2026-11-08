package com.yourproject.controller;

import com.yourproject.dto.OrderRequest;
import com.yourproject.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {
    @Autowired
    private SalesOrderService orderService;

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            return ResponseEntity.ok(orderService.createOrder(request));
        } catch (RuntimeException e) {
            // Trả về lỗi 400 (Bad Request) nếu như hết tồn kho hoặc lỗi logic
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

@GetMapping("/dashboard")
public ResponseEntity<?> getDashboardData() {
    // 1. Lấy danh sách 5 đơn hàng mới nhất
    List<SalesOrder> recentOrders = orderService.getAllOrders().stream()
                                                .limit(5)
                                                .toList();
    
    // 2. Tính doanh thu hôm nay
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    double dailySales = orderService.getAllOrders().stream()
        .filter(order -> order.getCreatedAt().isAfter(startOfDay) && "COMPLETED".equals(order.getStatus()))
        .mapToDouble(SalesOrder::getTotalAmount)
        .sum();

    // 3. Đếm số đơn chờ xử lý (nếu có trạng thái PENDING)
    long pendingOrders = orderService.getAllOrders().stream()
        .filter(order -> "PENDING".equals(order.getStatus()))
        .count();

    // Đóng gói dữ liệu trả về cho Frontend
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> stats = new HashMap<>();
    stats.put("dailySales", dailySales);
    stats.put("pendingOrders", pendingOrders);
    
    response.put("stats", stats);
    response.put("recentOrders", recentOrders);

    return ResponseEntity.ok(response);
}