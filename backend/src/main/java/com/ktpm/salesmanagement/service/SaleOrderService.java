package com.yourproject.service;

import com.yourproject.dto.OrderRequest;
import com.yourproject.dto.OrderItemRequest;
import com.yourproject.entity.Customer;
import com.yourproject.entity.Product;
import com.yourproject.entity.SalesOrder;
import com.yourproject.entity.SalesOrderDetail;
import com.yourproject.repository.CustomerRepository;
import com.yourproject.repository.ProductRepository;
import com.yourproject.repository.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesOrderService {
    @Autowired private SalesOrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;

    public List<SalesOrder> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public SalesOrder getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    @Transactional
    public SalesOrder createOrder(OrderRequest request) {
        SalesOrder order = new SalesOrder();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("COMPLETED");

        // 1. Kiểm tra và gán khách hàng
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isEmpty()) {
            Customer customer = customerRepository.findByPhone(request.getCustomerPhone())
                .orElseGet(() -> {
                    Customer newCus = new Customer();
                    newCus.setPhone(request.getCustomerPhone());
                    newCus.setName("Khách vãng lai");
                    return customerRepository.save(newCus);
                });
            order.setCustomer(customer);
        }

        double totalAmount = 0;
        List<SalesOrderDetail> details = new ArrayList<>();

        // 2. Xử lý từng món hàng, tính tiền và trừ kho
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho!");
            }
            
            // Trừ số lượng tồn kho
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            // Ghi nhận chi tiết đơn hàng
            SalesOrderDetail detail = new SalesOrderDetail();
            detail.setSalesOrder(order); 
            detail.setProduct(product);
            detail.setQuantity(itemReq.getQuantity());
            detail.setPrice(itemReq.getPrice());
            
            totalAmount += itemReq.getPrice() * itemReq.getQuantity();
            details.add(detail);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderDetails(details); 

        return orderRepository.save(order);
    }
}