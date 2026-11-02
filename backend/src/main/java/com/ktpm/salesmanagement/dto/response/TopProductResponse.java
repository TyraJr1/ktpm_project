package com.ktpm.salesmanagement.dto.response;

public record TopProductResponse(
        Long productId,
        String productCode,
        String productName,
        long totalQuantity
) {
}
