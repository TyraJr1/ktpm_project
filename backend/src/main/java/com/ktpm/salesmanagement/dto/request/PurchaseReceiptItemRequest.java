package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PurchaseReceiptItemRequest(
        @NotNull(message = "Sản phẩm không được để trống")
        Long productId,

        @NotNull(message = "Số lượng nhập không được để trống")
        @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0")
        Integer quantity,

        @NotNull(message = "Giá nhập không được để trống")
        @DecimalMin(value = "0.0", inclusive = true, message = "Giá nhập phải lớn hơn hoặc bằng 0")
        BigDecimal purchasePrice
) {
}
