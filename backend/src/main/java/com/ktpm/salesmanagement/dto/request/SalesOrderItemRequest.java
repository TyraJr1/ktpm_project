package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SalesOrderItemRequest(
        @NotNull(message = "Sản phẩm không được để trống")
        Long productId,

        @NotNull(message = "Số lượng mua không được để trống")
        @Min(value = 1, message = "Số lượng mua phải lớn hơn 0")
        Integer quantity
) {
}
