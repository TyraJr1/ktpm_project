package com.ktpm.salesmanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockOutboundRequest(
        @NotNull(message = "Sản phẩm xuất kho không được để trống")
        Long productId,

        @NotNull(message = "Số lượng xuất không được để trống")
        @Min(value = 1, message = "Số lượng xuất phải lớn hơn 0")
        Integer quantity,

        @NotBlank(message = "Lý do xuất kho không được để trống")
        @Size(max = 500, message = "Lý do xuất kho tối đa 500 ký tự")
        String reason
) {
}
