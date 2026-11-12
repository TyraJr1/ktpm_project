package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(
        @NotNull(message = "Sản phẩm kiểm kê / điều chỉnh không được để trống")
        Long productId,

        @NotNull(message = "Số lượng thực tế không được để trống")
        @Min(value = 0, message = "Số lượng thực tế phải là số nguyên không âm")
        Integer actualQuantity,

        @NotBlank(message = "Lý do điều chỉnh kho không được để trống")
        @Size(max = 500, message = "Lý do điều chỉnh tối đa 500 ký tự")
        String reason
) {
}
