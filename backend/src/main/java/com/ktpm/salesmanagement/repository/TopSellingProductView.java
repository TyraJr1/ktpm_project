package com.ktpm.salesmanagement.repository;

public interface TopSellingProductView {
    Long getProductId();
    String getProductCode();
    String getProductName();
    Long getTotalQuantity();
}
