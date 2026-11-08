package com.yourproject.dto;

import java.util.List;

public class OrderRequest {
    private String customerPhone;
    private List<OrderItemRequest> items;

    // Getters và Setters
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}