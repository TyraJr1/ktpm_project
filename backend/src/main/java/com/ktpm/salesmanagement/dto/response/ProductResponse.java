package com.ktpm.salesmanagement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String productCode,
        String productName,
        BigDecimal salePrice,
        Integer stockQuantity,
        String status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
