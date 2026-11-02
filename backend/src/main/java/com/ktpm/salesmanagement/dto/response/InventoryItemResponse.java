package com.ktpm.salesmanagement.dto.response;

public record InventoryItemResponse(
        Long productId,
        String productCode,
        String productName,
        String categoryName,
        Integer stockQuantity,
        String status
) {
}
